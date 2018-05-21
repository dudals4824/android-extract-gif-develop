package com.naver.hackday.android_extract_gif.recyclerViewObject;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.naver.hackday.android_extract_gif.R;
import com.naver.hackday.android_extract_gif.http.listener.LogoutButtonClick;
import com.naver.hackday.android_extract_gif.http.model.LoginInforItem;

import java.util.List;

public class LoginInforListAdapter extends RecyclerView.Adapter<LoginInforListAdapter.LoginItemViewHolder> {
    private List<LoginInforItem> loginList;
    private LogoutButtonClick listener;

    public static class LoginItemViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView loginInfo;
        private Button logoutBtn;

        private LoginItemViewHolder(View view, LogoutButtonClick listener) {
            super(view);
            title = (TextView) view.findViewById(R.id.login_infor_item_title);
            loginInfo = (TextView) view.findViewById(R.id.login_infor_item_state);
            logoutBtn = (Button) view.findViewById(R.id.login_infor_item_logout);

            logoutBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.logoutButtonClick(getAdapterPosition());
                }
            });
        }
    }

    public LoginInforListAdapter(List<LoginInforItem> loginList, LogoutButtonClick listener) {
        this.loginList = loginList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LoginItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.login_information_item, parent, false);

        LoginItemViewHolder holder = new LoginItemViewHolder(v, listener);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull LoginItemViewHolder holder, int position) {
        holder.title.setText(loginList.get(position).getTitle());
        if (loginList.get(position).isLogin()) {
            holder.loginInfo.setText("로그인 완료");
            holder.logoutBtn.setClickable(true);
        } else {
            holder.loginInfo.setText("로그인 필요");
            holder.logoutBtn.setClickable(false);
        }
    }

    @Override
    public int getItemCount() {
        return loginList.size();
    }
}
