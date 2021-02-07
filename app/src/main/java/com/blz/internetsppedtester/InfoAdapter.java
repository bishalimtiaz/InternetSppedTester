package com.blz.internetsppedtester;

import android.content.Context;
import android.content.SyncStats;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class InfoAdapter extends RecyclerView.Adapter<InfoAdapter.InfoViewHolder> {

    private final Context context;
    private List<Info> infoList;
    public InfoAdapter(Context context,  List<Info> infoList){
        this.context = context;
        this.infoList = infoList;
    }
    @NonNull
    @Override
    public InfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater mInflator = LayoutInflater.from(context);
        View view = mInflator.inflate(R.layout.device_info_item,parent,false);
        return new InfoAdapter.InfoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InfoViewHolder holder, int position) {
        holder.info_title.setText(infoList.get(position).getInfo_tittle());
        holder.info_val.setText(infoList.get(position).getInfo_val());
    }

    @Override
    public int getItemCount() {
        return infoList.size();
    }

    public List<Info> getInfoList() {
        return infoList;
    }

    public void setInfoList(List<Info> infoList) {
        this.infoList = infoList;
        notifyDataSetChanged();
    }

    public class InfoViewHolder extends RecyclerView.ViewHolder {
        TextView info_title,info_val;

        public InfoViewHolder(@NonNull View itemView) {
            super(itemView);
            info_title = itemView.findViewById(R.id.info_title);
            info_val = itemView.findViewById(R.id.info_val);
        }
    }
}
