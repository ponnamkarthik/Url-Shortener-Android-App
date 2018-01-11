package io.github.ponnamkarthik.urlshortner.dashboard;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.ponnamkarthik.urlshortner.R;

/**
 * Created by ponna on 11-01-2018.
 */

public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.ViewHolder> {

    private List<DashboardModel> models;
    private Context context;
    private DeleteInterface deleteUrl;

    public DashboardAdapter(Context context, List<DashboardModel> models, DeleteInterface deleteUrl) {
        this.context = context;
        this.models = models;
        this.deleteUrl = deleteUrl;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_url_item, null);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.textUrlTitle.setText(models.get(position).getTitle());
        holder.textShortUrl.setText(models.get(position).getShort_url());
        holder.textViews.setText(Integer.toString(models.get(position).getViews()));
        holder.textOriginalUrl.setText(models.get(position).getUrl());

        holder.buttonPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(models.get(position).getShort_url()));
                context.startActivity(intent);
            }
        });

        holder.buttonCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setClipboardText(models.get(position).getShort_url(), holder.buttonCopy);
            }
        });

        holder.buttonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                intent.putExtra(Intent.EXTRA_TEXT, models.get(position).getShort_url());
                context.startActivity(Intent.createChooser(intent, "Share link!"));
            }
        });

        holder.imageDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteUrl.deleteUrl(models.get(position).getUid(), models.get(position).getCode());
            }
        });
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_url_title)
        TextView textUrlTitle;
        @BindView(R.id.image_delete)
        ImageView imageDelete;
        @BindView(R.id.text_short_url)
        TextView textShortUrl;
        @BindView(R.id.text_views)
        TextView textViews;
        @BindView(R.id.text_original_url)
        TextView textOriginalUrl;
        @BindView(R.id.button_copy)
        Button buttonCopy;
        @BindView(R.id.button_preview)
        Button buttonPreview;
        @BindView(R.id.button_share)
        Button buttonShare;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    private void setClipboardText(String text, View view) {
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Copied Text", text);
            clipboard.setPrimaryClip(clip);
        }

        Snackbar.make(view, "Short Url Copied !",
                Snackbar.LENGTH_SHORT).show();
    }

    public void updateListData(List<DashboardModel> models) {
        this.models = models;
        notifyDataSetChanged();
    }
}
