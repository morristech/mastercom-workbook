package com.stypox.mastercom_workbook.view.holder;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.stypox.mastercom_workbook.R;
import com.stypox.mastercom_workbook.data.MarkData;
import com.stypox.mastercom_workbook.util.DateUtils;
import com.stypox.mastercom_workbook.util.MarkFormatting;

public class MarkDetailItemHolder extends ItemHolder<MarkData> {
    private final TextView valueView;
    private final TextView typeView;
    private final TextView subjectView;
    private final TextView descriptionView;
    private final TextView teacherDateView;

    private final Context context;


    public MarkDetailItemHolder(@NonNull View itemView, @Nullable ItemArrayAdapter<MarkData> adapter) {
        super(itemView, adapter);
        context = itemView.getContext();

        valueView = itemView.findViewById(R.id.value);
        typeView = itemView.findViewById(R.id.type);
        subjectView = itemView.findViewById(R.id.subject);
        descriptionView = itemView.findViewById(R.id.description);
        teacherDateView = itemView.findViewById(R.id.subtitle);
    }

    @Override
    public void updateItemData(MarkData data) {
        valueView.setText(MarkFormatting.valueRepresentation(data.getValue()));
        valueView.setTextColor(MarkFormatting.colorOf(context, data.getValue()));
        typeView.setText(MarkFormatting.typeRepresentation(context, data.getType()));
        subjectView.setText(data.getSubject());

        if (data.getDescription().isEmpty()) {
            descriptionView.setText("");
            descriptionView.setVisibility(View.GONE);
        } else {
            descriptionView.setText(data.getDescription());
            descriptionView.setVisibility(View.VISIBLE);
        }

        teacherDateView.setText(context.getResources().getString(R.string.two_strings,
                data.getTeacher(), DateUtils.formatDate(data.getDate())));
    }


    private static class Factory implements ItemHolderFactory<MarkData> {
        @Override
        public MarkDetailItemHolder buildItemHolder(@NonNull View itemView, @Nullable ItemArrayAdapter<MarkData> adapter) {
            return new MarkDetailItemHolder(itemView, adapter);
        }
    }

    private static final Factory factory = new Factory();

    public static Factory getFactory() {
        return factory;
    }
}
