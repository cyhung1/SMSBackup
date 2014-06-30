package com.example.smsbackup;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.example.smsbackup.SmsRecord;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.provider.ContactsContract.PhoneLookup;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

    public void backupMessage(View view) {
		TreeSet<SmsRecord> messages = new TreeSet<SmsRecord>();

		// Get the SMS inbox messages and write to file
		/* Messages in the inbox have date stamps in UTC that correspond to the receiver's LOCAL time.
		 * 
		 * In other words: A date stamp of Tue, 28 Feb 2012 14:51:25 GMT *actually* means
		 * Tue, 28 Feb 2012 14:51:25 EST if the recipient is in EST.
		 * 
		 * Sent messages are different. They are knowledgeable of the SENDER'S time zone.
		 * 
		 * In other words: Tue, 28 Feb 2012 14:51:25 GMT actually means the message was sent on
		 * 2/28/2012 9:51:25 AM GMT-5
		 */
		Uri inboxUri = Uri.parse("content://sms/inbox");
    	
    	ContentResolver cr = getContentResolver();
    	/*
    	 * TODO: I like the idea of specifying the desired fields. Once SMS is no longer
    	 * outside the SDK, I'll add them back.
    	String[] reqCols = new String [] {"_id", "thread_id", "address", "person", "date", "protocol",
    									"read", "priority", "status", "type", "callback_number",
    									"reply_path_present", "subject", "body", "service_center", "failure_cause",
    									"locked", "error_code", "stack_type", "seen", "sort_index"};
    	*/
    	Cursor cur = cr.query(inboxUri, null, null, null, null);

    	while (cur.moveToNext()) {
    		SmsRecord rec = new SmsRecord();
    		for(int ii=0; ii < cur.getColumnCount(); ii++) {
    			rec.setFieldByName(cur.getColumnName(ii), cur.getString(ii));
    		}

    		try {
    			messages.add(rec);
    		}
    		catch(Exception e) {
    			e.printStackTrace();
    		}
    	}
    	
    	cur.close();
    	
	try {
    	// Get the SMS sent messages and write to file
		Uri sentUri = Uri.parse("content://sms/sent");
    	
    	cur = cr.query(sentUri, null, null, null, null);

    	while (cur.moveToNext()) {
    		SmsRecord rec = new SmsRecord();

    		for(int ii=0; ii < cur.getColumnCount(); ii++) {
    			rec.setFieldByName(cur.getColumnName(ii), cur.getString(ii));
    		}
    		
    		try {
    			messages.add(rec);
    		}
    		catch (Exception e) {
    			e.printStackTrace();
    		}
    	}
    	
    	cur.close();
    	
    	// Put all contact display names in a map
    	TreeMap<String, String> contactMap = new TreeMap<String, String>();
    	for(SmsRecord smsRec: messages) {
    		if(contactMap.get(smsRec.address) == null) {
        		Uri lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(smsRec.address));
        		
        		String [] contCols = new String[] {PhoneLookup.DISPLAY_NAME};
        		cur = cr.query(lookupUri, contCols, null, null, null);
        		
        		if(cur.moveToFirst()) {
        			String contName = cur.getString(cur.getColumnIndex(PhoneLookup.DISPLAY_NAME));
        			contactMap.put(smsRec.address, contName);
        		}
        		
        		cur.close();
    		}
    	}

		// Write to XML file on external storage
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("SmsMessages");
		rootElement.setAttribute("count", Integer.toString(messages.size()));
		doc.appendChild(rootElement);
		
		for(SmsRecord smsRec: messages) {
    		Element msg = doc.createElement("sms");
    		
    		msg.setAttribute("id", smsRec.id);
    		msg.setAttribute("thread_id", smsRec.thread_id);
    		 // TODO: consider stripping '(, ), and -' out of address
    		msg.setAttribute("address", smsRec.address);
    		msg.setAttribute("date", smsRec.date);
    		msg.setAttribute("protocol", smsRec.protocol);
    		msg.setAttribute("read", smsRec.read);
    		msg.setAttribute("status", smsRec.status);
    		msg.setAttribute("type", smsRec.type);
    		msg.setAttribute("subject", smsRec.subject);
    		msg.setAttribute("body", smsRec.body);
    		msg.setAttribute("service_center", smsRec.service_center);
    		msg.setAttribute("locked", smsRec.locked);
    		msg.setAttribute("error_code", smsRec.error_code);
    		
    		// I don't know what the following fields are for, nor do they seem to have data
    		//msg.setAttribute("person", smsRec.person);
    		//msg.setAttribute("priority", smsRec.priority);
    		//msg.setAttribute("callback_number", smsRec.callback_number);
    		//msg.setAttribute("reply_path_present", smsRec.reply_path_present);
    		//msg.setAttribute("failure_cause", smsRec.failure_cause);
    		//msg.setAttribute("stack_type", smsRec.stack_type);
    		//msg.setAttribute("seen", smsRec.seen);
    		//msg.setAttribute("sort_index", smsRec.sort_index);
    		
    		String contName = contactMap.get(smsRec.address);
    		if(contName == null) {
    			contName = "(Unknown)";
    		}
    		msg.setAttribute("contact_name", contName);
    		
    		// Get readable date
    		Date date = new Date(Long.parseLong(smsRec.date));
			DateFormat format = new SimpleDateFormat("MMMM d, yyyy HH:mm:ss");
			if(Integer.parseInt(smsRec.type) == 2) {
				format.setTimeZone(TimeZone.getTimeZone("America/New_York"));
			}
			else {
				// Received messages in older Android versions seem to be GMT, but reflect local time
				//format.setTimeZone(TimeZone.getTimeZone("UTC"));
				
				// Newer versions (at least Ice Cream Sandwich) seem to be local time
				format.setTimeZone(TimeZone.getTimeZone("America/New_York"));
			}
    		String readableDate = new String();				
			readableDate = format.format(date);

			msg.setAttribute("readable_date", readableDate);
			
    		rootElement.appendChild(msg);
		}

    	Calendar date = Calendar.getInstance();

    	File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
    	
    	File file = new File(dir, "smsBackup_" + date.get(Calendar.YEAR) +
    						((date.get(Calendar.MONTH)+1) < 10 ? "0" : "") + (date.get(Calendar.MONTH)+1) +
    						((date.get(Calendar.DAY_OF_MONTH)) < 10 ? "0" : "") + date.get(Calendar.DAY_OF_MONTH) +
    						((date.get(Calendar.HOUR_OF_DAY)) < 10 ? "0" : "") + date.get(Calendar.HOUR_OF_DAY) +
    						((date.get(Calendar.MINUTE)) < 10 ? "0" : "") + date.get(Calendar.MINUTE) +
    						((date.get(Calendar.SECOND)) < 10 ? "0" : "") + date.get(Calendar.SECOND) +
    						".xml");
    	
    	TransformerFactory transformerFactory = TransformerFactory.newInstance();
    	Transformer transformer = transformerFactory.newTransformer();
    	DOMSource source = new DOMSource(doc);
    	StreamResult result = new StreamResult(file);
    	
    	transformer.setOutputProperty(OutputKeys.METHOD, "xml");
    	transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    	transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
    	transformer.transform(source, result);

	} catch (ParserConfigurationException e) {
		e.printStackTrace();
	} catch (TransformerConfigurationException e) {
		e.printStackTrace();
	} catch (TransformerException e) {
		e.printStackTrace();
	} catch (NullPointerException e) {
		e.printStackTrace();
	}
	
}
}
