package com.example.jakesetton.myfirstapp;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import me.everything.providers.android.browser.Bookmark;
import me.everything.providers.android.browser.BrowserProvider;
import me.everything.providers.android.browser.Search;
import me.everything.providers.android.calllog.Call;
import me.everything.providers.android.calllog.CallsProvider;
import me.everything.providers.android.telephony.Mms;
import me.everything.providers.android.telephony.Sms;
import me.everything.providers.android.telephony.TelephonyProvider;
import me.everything.providers.core.Data;

public class MessageRead {
    //ultimately unused class for running German's sentiment analysis classifier + other stats from calls and texts
    Context context;
    TelephonyProvider telephonyProvider = new TelephonyProvider(context);
    BrowserProvider browserProvider = new BrowserProvider(context);
    CallsProvider callsProvider = new CallsProvider(context);

    //Accessing SMS and MMS sent text (for "Connect")
    private List<Sms> smses = telephonyProvider.getSms(TelephonyProvider.Filter.SENT).getList();
    //if need be: private ArrayList<Sms> smses = (ArrayList) telephonyProvider.getSms(TelephonyProvider.Filter.SENT).getList();
    private List<Mms> mmses = telephonyProvider.getMms(TelephonyProvider.Filter.SENT).getList();

    public int oldsmssize = smses.size();
    public int oldmmssize = mmses.size();
    //N.B. there are also "conversations" (whatever that means) that you can access using this library

    //Accessing browser bookmarks and searches (for "Keep Learning")
    Data<Bookmark> bookmarkers = browserProvider.getBookmarks();        //N.B. with all this stuff if you type in e.g. search you'll see a telephony API thing come up too (in case the current method doesn't work...)
    Data<Search> searches = browserProvider.getSearches();

    public int oldbookmarkcount = bookmarkers.getList().size();
    public int oldsearchcount = searches.getList().size();

    //Accessing call logs (for "Give", "Connect")
    private Data<Call> calls = callsProvider.getCalls();   //N.B. keep typing in calls, callprovider etc. to see other options if this library doesn't work
    public int oldcallcount = calls.getList().size();

}
