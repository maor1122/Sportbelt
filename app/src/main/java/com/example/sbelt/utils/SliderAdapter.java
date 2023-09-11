package com.example.sbelt.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sbelt.R;

import java.util.List;

public class SliderAdapter extends RecyclerView.Adapter<SliderAdapter.SlideViewHolder> {
    private List<Slide> slides;

    public SliderAdapter(List<Slide> slides) {
        this.slides = slides;
    }

    @Override
    public SlideViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.slide_item_constrainer, parent, false);
        return new SlideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SlideViewHolder holder, int position) {
        holder.bind(slides.get(position));
    }

    @Override
    public int getItemCount() {
        return slides.size();
    }

    public class SlideViewHolder extends RecyclerView.ViewHolder {
        private TextView textTitle;
        private TextView textDescription;
        private ImageView imageIcon;

        public SlideViewHolder(View view) {
            super(view);
            textTitle = view.findViewById(R.id.textTitle);
            textDescription = view.findViewById(R.id.textDescription);
            imageIcon = view.findViewById(R.id.imageSlideIcon);
        }

        public void bind(Slide slide) {
            textTitle.setText(slide.title);
            textDescription.setText(slide.description);
            imageIcon.setImageResource(slide.icon);
        }
    }
}
