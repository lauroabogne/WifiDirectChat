package com.blogspot.justsimpleinfo.wifidirectchat;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;


public class ChatFragment extends Fragment implements View.OnClickListener {


    public final static String FRAGMENT_TAG = "ChatFragment";


    EditText mMessageInput;
    ImageButton mSendButton;
    ListView mMessageListview;

    List<JSONObject> messages = new ArrayList<>();

    public ChatFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        FrameLayout frameLayout = (FrameLayout) inflater.inflate(R.layout.fragment_chat, container, false);

        mMessageInput = (EditText) frameLayout.findViewById(R.id.message_input);
        mSendButton = (ImageButton) frameLayout.findViewById(R.id.send_message_button);
        mSendButton.setOnClickListener(this);
        mMessageListview = (ListView) frameLayout.findViewById(R.id.message_listview);
        mMessageListview.setAdapter(new MessageCustomAdapter(messages));
        return frameLayout;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    @Override
    public void onClick(View view) {


        InputMethodManager inputManager = (InputMethodManager)
                getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);

        String message = mMessageInput.getText()+"";
        mMessageInput.setText("");

        MainActivity mainActivity = (MainActivity) this.getActivity();

        if(mainActivity.isServer){



            try {

                JSONObject messageJSON = new JSONObject();
                messageJSON.put(MessageConstantVariables.FROM,MessageConstantVariables.FROM_SERVER);
                messageJSON.put(MessageConstantVariables.MESSAGE,message);

                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter( mainActivity.mServerChatService.mClientSocket.getOutputStream())), true);
                out.println(messageJSON);

                messages.add(messageJSON);

                MessageCustomAdapter messageCustomAdapter = (MessageCustomAdapter) mMessageListview.getAdapter();
                messageCustomAdapter.notifyDataSetChanged();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }else{


            try {

                JSONObject messageJSON = new JSONObject();
                messageJSON.put(MessageConstantVariables.FROM,MessageConstantVariables.FROM_CLIENT);
                messageJSON.put(MessageConstantVariables.MESSAGE,message);

                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter( mainActivity.mClientChatService.socket.getOutputStream())), true);
                out.println(messageJSON);

                messages.add(messageJSON);

                MessageCustomAdapter messageCustomAdapter = (MessageCustomAdapter) mMessageListview.getAdapter();
                messageCustomAdapter.notifyDataSetChanged();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Toast.makeText(this.getContext(), message, Toast.LENGTH_SHORT).show();

    }

    class  MessageCustomAdapter extends ArrayAdapter{

        List<JSONObject> messages;

        MessageCustomAdapter(List<JSONObject> messages ){
            super(ChatFragment.this.getContext(),R.layout.server_message_layout,messages);

            this.messages = messages;
        }



        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LinearLayout linearLayout = (LinearLayout) convertView;
            JSONObject messageJSONObject = (JSONObject) this.getItem(position);
            String from = "";
            String message = "";

            try {
                from = messageJSONObject.getString(MessageConstantVariables.FROM);
                message = messageJSONObject.getString(MessageConstantVariables.MESSAGE);
            } catch (JSONException e) {
                e.printStackTrace();
            }


            if(linearLayout == null){

                linearLayout = (LinearLayout) LayoutInflater.from(ChatFragment.this.getContext()).inflate(R.layout.server_message_layout, parent, false);


            }
            TextView messageIdentifierTextView = (TextView) linearLayout.findViewById(R.id.message_identifier);
            TextView messageTextView = (TextView) linearLayout.findViewById(R.id.message_display);

            if(((MainActivity) getActivity()).isServer){

                if(from == MessageConstantVariables.FROM_SERVER){

                    messageIdentifierTextView.setBackgroundResource(R.drawable.server_message_identifier);
                    messageIdentifierTextView.setText("YOU");

                }else{
                    messageIdentifierTextView.setBackgroundResource(R.drawable.client_message_identifier);
                    messageIdentifierTextView.setText("OTHER");
                }

            }else{

                if(from == MessageConstantVariables.FROM_CLIENT){
                    messageIdentifierTextView.setBackgroundResource(R.drawable.server_message_identifier);
                    messageIdentifierTextView.setText("YOU");

                }else{

                    messageIdentifierTextView.setBackgroundResource(R.drawable.client_message_identifier);
                    messageIdentifierTextView.setText("OTHER");
                }
            }

            messageTextView.setText(message);


            return linearLayout;
        }
    }
}
