package com.djzmo.bakingapp;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.djzmo.bakingapp.databinding.RecipeCardBinding;
import com.squareup.picasso.Picasso;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private RecipeInformation[] mData;
    private Context mContext;
    final private RecipeAdapterOnClickHandler mOnClickHandler;
    private RecipeCardBinding mBinding;

    public RecipeAdapter(Context c, RecipeAdapterOnClickHandler onClickHandler) {
        mContext = c;
        mOnClickHandler = onClickHandler;
    }

    @Override
    public RecipeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        mBinding = DataBindingUtil.inflate(inflater, R.layout.recipe_card, parent, false);

        return new RecipeViewHolder(mContext, mBinding.getRoot());
    }

    @Override
    public void onBindViewHolder(RecipeViewHolder holder, int position) {
        holder.bind(mData[position]);
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.length;
    }

    public void setData(RecipeInformation[] data) {
        mData = data;
        notifyDataSetChanged();
    }

    public RecipeInformation[] getData() {
        return mData;
    }

    public class RecipeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Context mParentContext;

        public RecipeViewHolder(Context c, View itemView) {
            super(itemView);
            mParentContext = c;
            itemView.setOnClickListener(this);
        }

        public void bind(RecipeInformation data) {
            mBinding.tvRecipeName.setText(data.name);
            mBinding.tvRecipeServings.setText(String.format(mParentContext.getString(R.string.for_n_servings), data.servings));
            if(data.image != null && data.image.length() > 0)
                Picasso.with(mParentContext).load(data.image).into(mBinding.ivRecipeImage);
            else mBinding.ivRecipeImage.setVisibility(View.GONE);
        }

        @Override
        public void onClick(View view) {
            mOnClickHandler.onClick(mData[getAdapterPosition()]);
        }
    }

    interface RecipeAdapterOnClickHandler {
        void onClick(RecipeInformation information);
    }

}
