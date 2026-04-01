package hr.ferit.teo_samarzija.picoblaze_simulator;

import static androidx.appcompat.content.res.AppCompatResources.getDrawable;

import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Locale;

public class AdapterForMachineCode
    extends RecyclerView.Adapter<AdapterForMachineCode.ViewHolder> {
  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                       int viewType) {
    View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.machine_code_directive_representation,
                             parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    if (position == 0) {
      holder.getAddressView().setText(R.string.address);
      holder.getAddressView().setTypeface(null, Typeface.BOLD);
      holder.getLineNumberView().setText(R.string.line_of_assembly);
      holder.getLineNumberView().setTypeface(null, Typeface.BOLD);
      holder.getMachineCodeView().setText(R.string.machine_code_directive);
      holder.getMachineCodeView().setTypeface(null, Typeface.BOLD);
      return;
    }
    AssembledProgram assembledProgram = AssembledProgram.getInstance();
    int index = -1;
    while (position > 0 && index < (1 << 12) - 1) {
      index++;
      if (assembledProgram.lineNumbers[index] != 0 ||
          assembledProgram.instructions[index] != 0)
        position--;
    }
    StringBuilder machineCodeString = new StringBuilder(
        Integer.toHexString(assembledProgram.instructions[index]));
    while (machineCodeString.length() < 5)
      machineCodeString.insert(0, "0");
    StringBuilder addressString = new StringBuilder(Integer.toHexString(index));
    while (addressString.length() < 3)
      addressString.insert(0, "0");
    holder.getAddressView().setText(addressString.toString());
    holder.getMachineCodeView().setText(machineCodeString.toString());
    holder.getLineNumberView().setText(
        String.format(Locale.US, "%d", assembledProgram.lineNumbers[index]));
    holder.getMachineCodeView().setTypeface(null, Typeface.NORMAL);
    holder.getLineNumberView().setTypeface(null, Typeface.NORMAL);
    holder.getAddressView().setTypeface(null, Typeface.NORMAL);
    if (assembledProgram.breakpoints.contains(assembledProgram.lineNumbers[index])
        && !assembledProgram.forbiddenBreakpoints.contains(index))
    {
        Log.d("PicoBlaze","There appears to be a breakpoint on the line " + assembledProgram.lineNumbers[index]+", as the breakpoints are "+assembledProgram.breakpoints.toString()+", and the forbidden breakpoints are "+assembledProgram.forbiddenBreakpoints.toString());
        holder.getAddressView().setBackground(getDrawable(holder.getAddressView().getContext(), R.drawable.table_background_marked));
        holder.getLineNumberView().setBackground(getDrawable(holder.getLineNumberView().getContext(), R.drawable.table_background_marked));
        holder.getMachineCodeView().setBackground(getDrawable(holder.getMachineCodeView().getContext(), R.drawable.table_background_marked));
    }
    else
    {
        holder.getAddressView().setBackground(getDrawable(holder.getAddressView().getContext(), R.drawable.table_background));
        holder.getLineNumberView().setBackground(getDrawable(holder.getLineNumberView().getContext(), R.drawable.table_background));
        holder.getMachineCodeView().setBackground(getDrawable(holder.getMachineCodeView().getContext(), R.drawable.table_background));
    }
  }

  @Override
  public int getItemCount() {
    int counter = 1;
    AssembledProgram assembledProgram = AssembledProgram.getInstance();
    for (int i = 0; i < assembledProgram.lineNumbers.length; i++) {
      if (assembledProgram.lineNumbers[i] != 0 ||
          assembledProgram.instructions[i] != 0)
        counter++;
    }
    return counter;
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    private final TextView addressView;
    private final TextView machineCodeView;
    private final TextView lineNumberView;
    public ViewHolder(View view) {
      super(view);
      addressView = view.findViewById(R.id.address);
      machineCodeView = view.findViewById(R.id.machineCodeDirective);
      lineNumberView = view.findViewById(R.id.lineNumber);
    }

    public TextView getAddressView() { return addressView; }
    public TextView getMachineCodeView() { return machineCodeView; }
    public TextView getLineNumberView() { return lineNumberView; }
  }
}
