package com.example.sherifhussien.phonalyzer;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by sherifhussien on 11/26/17.
 */

public class MessageAdapter extends ArrayAdapter<Message> {

    public MessageAdapter(Context context, ArrayList<Message> messages){
        super(context,0,messages); //0 as i will override the getView
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) 
    {
        Message currentMessage = getItem(position);

        View listItemView = convertView;

        if(listItemView == null)
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.message_item,parent,false);

        TextView messageView = (TextView) listItemView.findViewById(R.id.message_body);
        messageView.setText(currentMessage.getMessage());

        GradientDrawable bgShape = (GradientDrawable)messageView.getBackground();

        RelativeLayout.LayoutParams params= new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, 
                                                                            RelativeLayout.LayoutParams.WRAP_CONTENT);
        if(currentMessage.isSender())
        {
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.setMargins(0, 16, 16, 16);
            int color = Color.parseColor("#3385ff"); //Light Blue
            bgShape.setColor(color);

        }else
        {
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.setMargins(16, 16, 0, 16);
            int color = Color.parseColor("#0052cc"); // A little bit darker Blue
            bgShape.setColor(color);
        }
        messageView.setLayoutParams(params);

        return listItemView;
    }
}
