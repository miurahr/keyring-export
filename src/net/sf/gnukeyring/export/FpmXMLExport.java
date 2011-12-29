package net.sf.gnukeyring.export;

import net.sf.gnukeyring.KeyringEntry;
import net.sf.gnukeyring.KeyringLibrary;
import java.io.*;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * The FPM2 XML export for a keyring database.
 */

public class FpmXMLExport extends Export {
    public XMLExport() {
    }
    
    public void export() throws IOException {
	writer.write("\uFEFF");
	writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	writer.write("<FPM full_version=\"00.75.00\" min_version=\"00.75.00\" display_version=\"0.75\">\n");
	writer.write("<KeyInfo cipher=\"none\"/>");
	writer.write("<PasswordList>\n");
	exportEntries();
	writer.write("</PasswordList>\n");
	writer.write("</FPM>\n");
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
	
	for (Iterator i = entries.iterator(); i.hasNext(); ) {
	    KeyringEntry entry = (KeyringEntry) i.next();

	    writer.write("<PasswordItem>\n");
	    writer.write("  <title>"+xmlencode(entry.getName())+"</title>\n");
	    String category = (String) entry.getCategory();
	    String account = (String) entry.getField("Account");
	    String password = (String) entry.getField("Password");
	    String notes = (String) entry.getField("Notes");
	    if (account != null)
		writer.write("  <user>"+xmlencode(account)+"</user>\n");
	    if (password != null)
		writer.write("  <password>"+xmlencode(password)+"</password>\n");
	    if (notes != null)
		writer.write("  <notes>"+xmlencode(notes)+"</notes>\n");
	    if (category != null)
		writer.write("  <category>"+xmlencode(category)+"</category>\n");
	    writer.write("<launcher></launcher>\n");
	    writer.write("</PasswordItem>\n");
	}
    }
}
