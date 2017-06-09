package rudiment.alaramapp.receiver;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.content.WakefulBroadcastReceiver;

import java.util.ArrayList;

import rudiment.alaramapp.bean.ContactBean;

public class ContentUpdateReceiver extends WakefulBroadcastReceiver {

    private ArrayList<ContactBean> contactList;
    Cursor cursor;
    int counter = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        //send contact to server...and save in file
        //      getContacts(context);
    }

    public void getContacts(Context context) {

        contactList = new ArrayList<>();

        String phoneNumber = null;
        String email = null;

        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;

        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;

        Uri EmailCONTENT_URI = ContactsContract.CommonDataKinds.Email.CONTENT_URI;
        String EmailCONTACT_ID = ContactsContract.CommonDataKinds.Email.CONTACT_ID;
        String DATA = ContactsContract.CommonDataKinds.Email.DATA;

        ContactBean contactBean;

        ContentResolver contentResolver = context.getContentResolver();

        cursor = contentResolver.query(CONTENT_URI, null, null, null, null);

        // Iterate every contact in the phone
        if (cursor.getCount() > 0) {

            counter = 0;
            while (cursor.moveToNext()) {
                contactBean = new ContactBean();

                String contact_id = cursor.getString(cursor.getColumnIndex(_ID));
                String name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));

                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(HAS_PHONE_NUMBER)));

                if (hasPhoneNumber > 0) {
                    contactBean.name = name;

                    //This is to read multiple phone numbers associated with the same contact
                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[]{contact_id}, null);

                    ArrayList<ContactBean.Number> numbersItem = new ArrayList<>();
                    while (phoneCursor.moveToNext()) {
                        phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
                        ContactBean.Number number = new ContactBean.Number();
                        number.isRegistered = "0";
                        number.phone = phoneNumber;
                        numbersItem.add(number);
                    }
                    contactBean.numbers = numbersItem;

                    phoneCursor.close();

                    // Read every email id associated with the contact
                    Cursor emailCursor = contentResolver.query(EmailCONTENT_URI, null, EmailCONTACT_ID + " = ?", new String[]{contact_id}, null);

                    ArrayList<String> emailsItem = new ArrayList<>();
                    while (emailCursor.moveToNext()) {
                        email = emailCursor.getString(emailCursor.getColumnIndex(DATA));
                        emailsItem.add(email);
                    }
                   // contactBean.emails = emailsItem;
                    emailCursor.close();
                }
                // Add the contact to the ArrayList
                contactList.add(contactBean);
            }

            // ListView has to be updated using a ui thread

        }
    }
}
