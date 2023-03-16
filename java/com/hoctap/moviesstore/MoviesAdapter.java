package com.hoctap.moviesstore;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class MoviesAdapter extends BaseAdapter {
    private MoviesActivity context;
    private int laout;
    private List<Movies> moviesList;

    /**
     * Hàm tạo đối tượng MoviesAdapter
     * @param context
     * @param laout
     * @param moviesList
     */
    public MoviesAdapter(MoviesActivity context, int laout, List<Movies> moviesList) {
        this.context = context;
        this.laout = laout;
        this.moviesList = moviesList;
    }

    @Override
    public int getCount() {
        return moviesList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public class ViewHolder{
        ImageView imgSource;
        TextView tvTitle, tvPrice;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            // Tạo đối tượng ViewHolder
            viewHolder = new ViewHolder();
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(laout, null);
            // Ánh xạ các view
            viewHolder.imgSource = convertView.findViewById(R.id.imgMovies);
            viewHolder.tvTitle = convertView.findViewById(R.id.tvTitle);
            viewHolder.tvPrice = convertView.findViewById(R.id.tvPrice);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Movies movies = moviesList.get(position);
        // Gắn giá trị cho các view
        viewHolder.tvTitle.setText(movies.getTitle());
        viewHolder.tvPrice.setText(movies.getPrice());
        Picasso.get().load(movies.getImgSource()).into(viewHolder.imgSource);

        // Bắt sự kiện khi người dùng ấn giữ vào tấm hình

        viewHolder.imgSource.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                context.shareImage(position);
                return false;
            }
        });

        return convertView;
    }
}
