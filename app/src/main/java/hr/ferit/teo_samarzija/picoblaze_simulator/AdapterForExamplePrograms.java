package hr.ferit.teo_samarzija.picoblaze_simulator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import java.util.List;

public class AdapterForExamplePrograms
    extends RecyclerView.Adapter<AdapterForExamplePrograms.ViewHolder> {

  private final List<ExampleProgram> exampleProgramList;
  private final View.OnClickListener clickListener;
  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                       int viewType) {
    View view =
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.example_program_representation, parent, false);
    view.setOnClickListener(clickListener);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    holder.getTextView().setText(exampleProgramList.get(position).name);
    Picasso.get()
        .load(exampleProgramList.get(position).image)
        .into(holder.getImageView());
    holder.getImageView().setContentDescription(
        exampleProgramList.get(position).image_alt);
  }

  @Override
  public int getItemCount() {
    return exampleProgramList.size();
  }

  public AdapterForExamplePrograms(List<ExampleProgram> newList,
                                   View.OnClickListener clickListener) {
    exampleProgramList = newList;
    this.clickListener = clickListener;
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    private final TextView textView;
    private final ImageView imageView;

    public ViewHolder(View view) {
      super(view);
      textView = (TextView)view.findViewById(R.id.name_of_the_example);
      imageView = view.findViewById(R.id.icon);
    }

    public TextView getTextView() { return textView; }

    public ImageView getImageView() { return imageView; }
  }
}
