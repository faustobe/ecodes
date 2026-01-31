package it.faustobe.ecodes.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import it.faustobe.ecodes.R;
import it.faustobe.ecodes.model.Product;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private List<Product> products;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Product product);
    }

    public ProductAdapter(List<Product> products, OnItemClickListener listener) {
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);
        holder.productNameText.setText(product.getProductName());
        holder.barcodeText.setText("Barcode: " + product.getBarcode());

        int count = product.getECodesCount();
        Context context = holder.itemView.getContext();
        holder.eCodesCountText.setText(count == 1
            ? context.getString(R.string.additive_count_singular, count)
            : context.getString(R.string.additive_count_plural, count));

        holder.itemView.setOnClickListener(v -> listener.onItemClick(product));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void updateData(List<Product> newProducts) {
        this.products = newProducts;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productNameText, barcodeText, eCodesCountText;

        ViewHolder(View itemView) {
            super(itemView);
            productNameText = itemView.findViewById(R.id.productNameText);
            barcodeText = itemView.findViewById(R.id.barcodeText);
            eCodesCountText = itemView.findViewById(R.id.eCodesCountText);
        }
    }
}
