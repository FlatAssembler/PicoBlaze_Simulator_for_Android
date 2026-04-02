package hr.ferit.teo_samarzija.picoblaze_simulator;

import static android.graphics.Typeface.*;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterForRegisterDumping extends RecyclerView.Adapter<AdapterForRegisterDumping.ViewHolder>
{
    @NonNull
    @Override
    public AdapterForRegisterDumping.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                               int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.register_representation,
                        parent, false);
        return new AdapterForRegisterDumping.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position == 0) {
            holder.getRegisterNameView().setText("Register Name");
            holder.getRegisterNameView().setTypeface(null, BOLD);
            holder.getRegbankAView().setText("Regbank A");
            holder.getRegbankAView().setTypeface(null, BOLD);
            holder.getRegbankBView().setText("Regbank B");
            holder.getRegbankBView().setTypeface(null, BOLD);
            return;
        }
        Simulator simulator = Simulator.getInstance();
        holder.getRegisterNameView().setText(String.format("s%x", position - 1));
        holder.getRegbankAView().setText(String.format("%02x",simulator.registers[0][position - 1]));
        holder.getRegbankBView().setText(String.format("%02x",simulator.registers[1][position - 1]));
        holder.getRegisterNameView().setTypeface(null, NORMAL);
        holder.getRegbankAView().setTypeface(null, NORMAL);
        holder.getRegbankBView().setTypeface(null, NORMAL);
    }

    @Override
    public int getItemCount() {
        return 17;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView registerName;
        private final TextView regbankA;
        private final TextView regbankB;
        public ViewHolder(View view) {
            super(view);
            registerName = view.findViewById(R.id.register_name);
            regbankA = view.findViewById(R.id.regbank_a);
            regbankB = view.findViewById(R.id.regbank_b);
        }

        public TextView getRegisterNameView() { return registerName; }
        public TextView getRegbankAView() { return regbankA; }
        public TextView getRegbankBView() { return regbankB; }
    }
}
