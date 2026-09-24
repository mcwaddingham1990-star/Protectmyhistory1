package com.stuffapp.protectmyhistory;

import android.accessibilityservice.AccessibilityService;
import android.text.InputType;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.*;
import java.util.regex.*;

public class MonitorAccessibilityService extends AccessibilityService {
    private static final Pattern EMAIL = Pattern.compile("(?i)\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}\\b");
    private static final Pattern PHONE = Pattern.compile("(?<!\\d)(?:\\+?1[ .-]?)?(?:\\(?[2-9]\\d{2}\\)?[ .-]?)?[2-9]\\d{2}[ .-]?\\d{4}(?!\\d)");
    private final Map<String,String> last = new HashMap<>();

    @Override public void onAccessibilityEvent(AccessibilityEvent e) {
        if (e == null || e.getPackageName() == null) return;
        String pkg = e.getPackageName().toString();
        if (pkg.equals(getPackageName())) return;
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;
        String mode = detectPrivate(root) ? "private" : "regular";
        scan(root, pkg, mode, 0);
    }

    private void scan(AccessibilityNodeInfo n, String pkg, String mode, int depth) {
        if (n == null || depth > 18) return;
        CharSequence raw = n.getText();
        String text = raw == null ? "" : raw.toString().trim();
        String id = n.getViewIdResourceName() == null ? "" : n.getViewIdResourceName();
        String hint = n.getHintText() == null ? "" : n.getHintText().toString();
        int input = n.getInputType();
        boolean secret = n.isPassword() || (input & InputType.TYPE_TEXT_VARIATION_PASSWORD) != 0 || hint.toLowerCase().contains("password") || hint.toLowerCase().contains("verification code");
        if (!secret && !text.isEmpty()) {
            if (isBrowser(pkg) && isAddressNode(id, hint) && looksLikeUrl(text)) emit("URL_" + mode.toUpperCase(), pkg, normalizeUrl(text), id);
            Matcher em = EMAIL.matcher(text); while (em.find()) emit(n.isEditable()?"EMAIL_TYPED":"EMAIL_SEEN", pkg, em.group(), id);
            Matcher ph = PHONE.matcher(text); while (ph.find()) {
                String digits = ph.group().replaceAll("\\D", "");
                if (digits.length() >= 7 && digits.length() <= 15) emit(n.isEditable()?"PHONE_TYPED":"PHONE_SEEN", pkg, ph.group(), id);
            }
            if ((pkg.contains("contacts") || pkg.contains("dialer")) && (n.isFocused() || n.isClicked() || n.isEditable())) emit("CONTACT_ACTIVITY", pkg, text, id);
        }
        for (int i=0;i<n.getChildCount();i++) scan(n.getChild(i), pkg, mode, depth+1);
    }
    private void emit(String kind,String pkg,String value,String slot){String k=kind+"|"+pkg+"|"+slot;if(value.equals(last.get(k)))return;last.put(k,value);HistoryStore.add(this,kind,pkg,value);}
    private boolean isBrowser(String p){return p.equals("com.android.chrome")||p.equals("com.microsoft.emmx")||p.contains("firefox")||p.contains("browser");}
    private boolean isAddressNode(String id,String hint){String s=(id+" "+hint).toLowerCase();return s.contains("url_bar")||s.contains("location_bar")||s.contains("address")||s.contains("search_box");}
    private boolean looksLikeUrl(String s){String x=s.toLowerCase();return x.startsWith("http://")||x.startsWith("https://")||x.startsWith("www.")||x.matches(".*[a-z0-9-]+\\.[a-z]{2,}.*");}
    private String normalizeUrl(String s){return s.startsWith("http")?s:"https://"+s;}
    private boolean detectPrivate(AccessibilityNodeInfo root){StringBuilder b=new StringBuilder();collect(root,b,0);String s=b.toString().toLowerCase();return s.contains("incognito")||s.contains("inprivate")||s.contains("private browsing");}
    private void collect(AccessibilityNodeInfo n,StringBuilder b,int d){if(n==null||d>7||b.length()>5000)return;if(n.getText()!=null)b.append(' ').append(n.getText());if(n.getContentDescription()!=null)b.append(' ').append(n.getContentDescription());for(int i=0;i<n.getChildCount();i++)collect(n.getChild(i),b,d+1);}
    @Override public void onInterrupt() {}
}
