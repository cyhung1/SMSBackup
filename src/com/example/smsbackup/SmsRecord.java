package com.example.smsbackup;

import com.example.smsbackup.SmsRecord;

public class SmsRecord implements Comparable<SmsRecord> {
	public String id;
	public String thread_id;
	public String address;
	public String person;
	public String date;
	public String protocol;
	public String read;
	public String priority;
	public String status;
	public String type;
	public String callback_number;
	public String reply_path_present;
	public String subject;
	public String body;
	public String service_center;
	public String failure_cause;
	public String locked;
	public String error_code;
	public String stack_type;
	public String seen;
	public String sort_index;
	
	public SmsRecord() {
		id = thread_id = address = person = date = protocol = read = priority = status = type =
				callback_number = reply_path_present = subject = body = service_center = 
				failure_cause = locked = error_code = stack_type = seen = sort_index = "";
	}
	
	public String getString() {
		String str = "";
		str += "id: " + id + "; thread_id: " + thread_id + "; address: " + address + "; person: " +
				person + "; date: " + date + "; protocol: " + protocol + "; read: " + read + 
				"; priority: " + priority + "; status: " + status + "; type: " + type +
				"; callback_number: " + callback_number + "; reply_path_present: " + reply_path_present +
				"; subject: " + subject + "; body: " + body + "; service_center: " + service_center +
				"; failure_cause: " + failure_cause + "; locked: " + locked + "; error_code: " +
				error_code + "; stack_type: " + stack_type + "; seen: " + seen + "; sort_index: " + sort_index;
		
		return str;
	}
	
	public void setFieldByName(String field, String value) {
		if(value == null) {
			// Default the string value
			value = "null";
		}
		
		if(field.equals("_id")) {
			this.id = value;
		}
		else if(field.equals("thread_id")) {
			this.thread_id = value;
		}
		else if(field.equals("address")) {
			this.address = value;
		}
		else if(field.equals("person")) {
			this.person = value;
		}
		else if(field.equals("date")) {
			this.date = value;
		}
		else if(field.equals("protocol")) {
			if(value.equals("null")) {
				value = "0"; // Copied this from SMS Backup & Restore, field always seems to be null
			}
			this.protocol = value;
		}
		else if(field.equals("read")) {
			this.read = value;
		}
		else if(field.equals("priority")) {
			this.priority = value;
		}
		else if(field.equals("status")) {
			this.status = value;
		}
		else if(field.equals("type")) {
			this.type = value;
		}
		else if(field.equals("callback_number")) {
			this.callback_number = value;
		}
		else if(field.equals("reply_path_present")) {
			this.reply_path_present = value;
		}
		else if(field.equals("subject")) {
			this.subject = value;
		}
		else if(field.equals("body")) {
			this.body = value;
		}
		else if(field.equals("service_center")) {
			this.service_center = value;
		}
		else if(field.equals("failure_cause")) {
			this.failure_cause = value;
		}
		else if(field.equals("locked")) {
			this.locked = value;
		}
		else if(field.equals("error_code")) {
			this.error_code = value;
		}
		else if(field.equals("stack_type")) {
			this.stack_type = value;
		}
		else if(field.equals("seen")) {
			this.seen = value;
		}
		else if(field.equals("sort_index")) {
			this.sort_index = value;
		}
	}
	
	public int compareTo(SmsRecord other) {
		if(Integer.parseInt(this.id) == Integer.parseInt(other.id)) {
			return 0;
		}
		else if(Integer.parseInt(this.id) < Integer.parseInt(other.id)) {
			return -1;
		}
		return 1;
	}
}
