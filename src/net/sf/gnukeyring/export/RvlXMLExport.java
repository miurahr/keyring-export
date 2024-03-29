package net.sf.gnukeyring.export;

import net.sf.gnukeyring.KeyringEntry;
import net.sf.gnukeyring.KeyringLibrary;
import java.io.*;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * The Revelation XML export for a keyring database.
 * a fork of XMLExport.java
 */

public class RvlXMLExport extends Export {
    public RvlXMLExport() {
    }
    
    public void export() throws IOException {
	writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	writer.write("<revelationdata version=\"0.4.11\" dataversion=\"1\">\n");
	exportEntries();
	writer.write("</revelationdata>\n");
	writer.close();
    }

    public String xmlencode(String str) {
	StringBuffer sb = new StringBuffer();
	for (int i = 0; i < str.length(); i++) {
	    char c = str.charAt(i);
	    switch (c) {
		case '&':
		    sb.append("&amp;");
		    break;
		case '<':
		    sb.append("&lt;");
		    break;
		case '>':
		    sb.append("&gt;");
		    break;
		default:
		    sb.append(c);
	    }
	}
	return sb.toString();
    }

    public void exportEntries() throws IOException {
	List entries = keylib.getEntries();
	Set categories= keylib.getCategories();
	Date now = new Date();
	String nowString = String.valueOf((long)(now.getTime()/1000L));

	for (Iterator i = categories.iterator(); i.hasNext(); ) {
		Object o = i.next();
        if (o != null) {
        String c = o.toString();

		writer.write("<entry type=\"folder\">\n");
	    writer.write("  <name>"+xmlencode(c)+"</name>\n");
		writer.write("  <description></description>\n");
		writer.write("  <updated>"+nowString+"</updated>\n");
	
		for (Iterator j = entries.iterator(); j.hasNext(); ) {
	    	KeyringEntry entry = (KeyringEntry) j.next();
	   	    String category = (String) entry.getCategory();

            if (category.equals(c)) {
	    	writer.write("  <entry type=\"generic\">\n");
	    	writer.write("    <name>"+xmlencode(entry.getName())+"</name>\n");
	    	String account = (String) entry.getField("Account");
			String password = (String) entry.getField("Password");
	    	Date   changed = (Date) entry.getField("Changed");
	    	String notes = (String) entry.getField("Notes");
	    	if (notes != null) {
                if ((password !=null) && notes.equals(password)) {
			    writer.write("    <description></description>\n");
                } else {
			    writer.write("    <description>"+xmlencode(notes)+"</description>\n");
                }
            }
	    	if (changed != null) {
			    String updated = String.valueOf((long)(changed.getTime()/1000L));
			    writer.write("    <updated>"+updated+"</updated>\n");
            }
	    	if (account != null)
			    writer.write("    <field id=\"generic-username\">"+xmlencode(account)+"</field>\n");
	    	if (password != null)
			    writer.write("    <field id=\"generic-password\">"+xmlencode(password)+"</field>\n");
	    	    writer.write("  </entry>\n");
            }
		}
        }
   	writer.write("</entry>\n");
	}
    }
}
