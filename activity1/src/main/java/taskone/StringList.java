package taskone;

import java.util.List;
import java.util.ArrayList;

class StringList {
    
    List<String> strings = new ArrayList<String>();

    public void add(String str) {
        int pos = strings.indexOf(str);
        if (pos < 0) {
            strings.add(str);
        }
    }
	
	public void pop() {
        int index = 0;
		strings.remove(index);
    }
	
	public String stringAt() {
        //return string at position
		return strings.get(0);
    }
	
	public static final <String> void swap (String[] a, int i, int j) {
	  String t = a[i];
	  a[i] = a[j];
	  a[j] = t;
	}

    public boolean contains(String str) {
        return strings.indexOf(str) >= 0;
    }

    public int size() {
        return strings.size();
    }

    public String toString() {
        return strings.toString();
    }
}