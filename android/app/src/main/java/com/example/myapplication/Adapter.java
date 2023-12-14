package com.example.myapplication;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Item;
import com.example.myapplication.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    private LayoutInflater inflater;
    private List<Item> data;
    private Adapter a;
    private CommandService mService;

    public Adapter(Context context, List<Item> list, CommandService ser) {
        this.inflater = LayoutInflater.from(context);
        this.data = list;
        this.mService = ser;
    }
    public void setAdapter(Adapter aa){
        this.a = aa;
    }
    public void setData(List<Item> l){
        this.data = l;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Item item = data.get(position);
        if(item.getshName().equals("Ничего не найдено")){
            holder.leftText.setText(item.getshName());
            holder.dotsMenu.setVisibility(View.INVISIBLE);
            return;
        }
        holder.leftText.setText(item.getName() + " (" + item.getshName() + ")");
        holder.rightText.setText(String.valueOf(item.getPrice()));

        holder.dotsMenu.setOnClickListener(v -> {
            showPopupMenu(v, position);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
    public void showPopupMenu(View view, int position) {
        try{
            // Создание и настройка попап меню
            PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
            popupMenu.inflate(R.menu.popup_menu);
            if(data.get(position).getFavor()){
                popupMenu.getMenu().removeItem(R.id.menu_item2);
                popupMenu.getMenu().add(0, R.id.menu_item2, 0, "Удалить из избранного");
            }else{
                popupMenu.getMenu().removeItem(R.id.menu_item2);
                popupMenu.getMenu().add(0, R.id.menu_item2, 0, "Добавить в избранное");
            }
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    int temp = menuItem.getItemId();
                    if(temp == R.id.menu_item2){//избранное
                        String ss = menuItem.getTitle().toString();
                        if(ss.equals("Добавить в избранное")){
                            data.get(position).setFavor(true);
                            //Log.d("ddw","в избранное добавить " + data.get(position).getshName() +":" + data.get(position).getName());
                            mService.sendCommand("add_favor " + data.get(position).getshName());
                        }else{
                            data.get(position).setFavor(false);
                            mService.sendCommand("rem_favor " + data.get(position).getshName());

                            //data.remove(position);
                        }
                        Log.d("ddw", "" + data.get(position).getFavor() + " pos:" + position);
                        return true;
                    } else if (temp == R.id.menu_item1) {//увед
                        Log.d("ddw", "нажата 2");
                        return true;
                    }else{
                        return false;
                    }
                }
            });
            popupMenu.show();
        }catch (Exception e){
            Log.d("ddw", e.getMessage());
        }

    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView leftText;
        TextView rightText;
        ImageButton dotsMenu;

        public ViewHolder(View itemView) {
            super(itemView);

            leftText = itemView.findViewById(R.id.left_text_item);
            rightText = itemView.findViewById(R.id.right_text_item);
            try{
                dotsMenu = itemView.findViewById(R.id.dots_menu);

            }catch (Exception e)
            {
                Log.d("ddw", e.getMessage());
            }
        }
    }

}