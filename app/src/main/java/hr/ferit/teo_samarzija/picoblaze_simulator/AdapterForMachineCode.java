package hr.ferit.teo_samarzija.picoblaze_simulator;

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
      holder.getLineNumberView().setText(R.string.line_of_assembly);
      holder.getMachineCodeView().setText(R.string.machine_code_directive);
      return;
    }
    AssembledProgram assembledProgram = AssembledProgram.getInstance();
    int index = -1;
    while (position > 0 && index < (1 << 12) - 1) {
      index++;
      if (assembledProgram.instructions[index] != 0)
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
  }

  @Override
  public int getItemCount() {
    int counter = 1;
    AssembledProgram assembledProgram = AssembledProgram.getInstance();
    for (int directive : assembledProgram.instructions) {
      if (directive != 0)
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
