package it.faustobe.ecodes.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import it.faustobe.ecodes.R;
import it.faustobe.ecodes.data.ProfileManager;
import it.faustobe.ecodes.model.Additive;
import java.util.List;

public class AdditiveAdapter extends RecyclerView.Adapter<AdditiveAdapter.ViewHolder> {
    private List<Additive> additives;
    private OnItemClickListener listener;
    private ProfileManager profileManager;

    public interface OnItemClickListener {
        void onItemClick(Additive additive);
    }

    public AdditiveAdapter(List<Additive> additives, OnItemClickListener listener) {
        this.additives = additives;
        this.listener = listener;
        this.profileManager = null;
    }

    public AdditiveAdapter(List<Additive> additives, OnItemClickListener listener, ProfileManager profileManager) {
        this.additives = additives;
        this.listener = listener;
        this.profileManager = profileManager;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_additive, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Additive additive = additives.get(position);
        holder.codeText.setText(additive.getCode());
        holder.nameText.setText(additive.getName());
        holder.classificationText.setText(getClassificationText(holder.itemView.getContext(), additive.getClassification()));
        holder.itemView.setOnClickListener(v -> listener.onItemClick(additive));

        // Setup danger bar
        setupDangerBar(holder, additive.getClassification());

        // Check profile compliance
        if (profileManager != null && profileManager.hasActiveProfile()) {
            String compliance = profileManager.checkAdditiveCompliance(additive);
            if (compliance != null) {
                holder.profileAlertBadge.setText(compliance);
                holder.profileAlertBadge.setVisibility(View.VISIBLE);
            } else {
                holder.profileAlertBadge.setVisibility(View.GONE);
            }
        } else {
            holder.profileAlertBadge.setVisibility(View.GONE);
        }
    }

    private void setupDangerBar(ViewHolder holder, String classification) {
        // Get danger level (0.0 to 1.0) and color
        float dangerLevel;
        int barColor;

        if (classification == null) classification = "N";

        switch (classification) {
            case "A": // Non-toxic
                dangerLevel = 0.12f;
                barColor = 0xFF4CAF50; // Green
                break;
            case "B": // Intolerance risk
                dangerLevel = 0.28f;
                barColor = 0xFF8BC34A; // Light green
                break;
            case "C": // Suspect
                dangerLevel = 0.45f;
                barColor = 0xFFFFEB3B; // Yellow
                break;
            case "D": // Strong toxicity suspect
                dangerLevel = 0.65f;
                barColor = 0xFFFF9800; // Orange
                break;
            case "E": // Dangerous
                dangerLevel = 0.85f;
                barColor = 0xFFF44336; // Red
                break;
            case "F": // Not allowed in EU
                dangerLevel = 1.0f;
                barColor = 0xFFB71C1C; // Dark red
                break;
            case "N": // Neutral
            default:
                dangerLevel = 0.08f;
                barColor = 0xFF9E9E9E; // Gray
                break;
        }

        final float level = dangerLevel;
        final int color = barColor;

        // Apply bar styling immediately if view is already laid out (recycled view)
        // Otherwise use post() to wait for layout
        if (holder.dangerBarTrack.getWidth() > 0) {
            applyDangerBar(holder, level, color);
        } else {
            holder.dangerBarTrack.post(() -> applyDangerBar(holder, level, color));
        }
    }

    private void applyDangerBar(ViewHolder holder, float level, int color) {
        int trackWidth = holder.dangerBarTrack.getWidth();
        if (trackWidth <= 0) return;

        int fillWidth = (int) (trackWidth * level);

        ViewGroup.LayoutParams params = holder.dangerBarFill.getLayoutParams();
        params.width = fillWidth;
        holder.dangerBarFill.setLayoutParams(params);

        // Create rounded drawable for fill
        GradientDrawable fillDrawable = new GradientDrawable();
        fillDrawable.setShape(GradientDrawable.RECTANGLE);
        fillDrawable.setCornerRadius(8f);
        fillDrawable.setColor(color);
        holder.dangerBarFill.setBackground(fillDrawable);
    }

    @Override
    public int getItemCount() {
        return additives.size();
    }

    public void updateData(List<Additive> newAdditives) {
        this.additives = newAdditives;
        notifyDataSetChanged();
    }

    private String getClassificationText(Context context, String classification) {
        if (classification == null) {
            return context.getString(R.string.classification_unknown_short);
        }
        switch (classification) {
            case "A": return context.getString(R.string.classification_a_short);
            case "B": return context.getString(R.string.classification_b_short);
            case "C": return context.getString(R.string.classification_c_short);
            case "D": return context.getString(R.string.classification_d_short);
            case "E": return context.getString(R.string.classification_e_short);
            case "F": return context.getString(R.string.classification_f_short);
            case "N": return context.getString(R.string.classification_n_short);
            default: return context.getString(R.string.classification_unknown_short);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView codeText, nameText, classificationText, profileAlertBadge;
        View dangerBarTrack, dangerBarFill;

        ViewHolder(View itemView) {
            super(itemView);
            codeText = itemView.findViewById(R.id.codeText);
            nameText = itemView.findViewById(R.id.nameText);
            classificationText = itemView.findViewById(R.id.classificationText);
            profileAlertBadge = itemView.findViewById(R.id.profileAlertBadge);
            dangerBarTrack = itemView.findViewById(R.id.dangerBarTrack);
            dangerBarFill = itemView.findViewById(R.id.dangerBarFill);
        }
    }
}
