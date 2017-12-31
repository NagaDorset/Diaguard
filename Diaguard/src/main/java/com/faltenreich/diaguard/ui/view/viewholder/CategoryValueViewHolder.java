package com.faltenreich.diaguard.ui.view.viewholder;

import android.view.View;
import android.widget.TextView;

import com.faltenreich.diaguard.R;
import com.faltenreich.diaguard.adapter.list.ListItemCategoryValue;
import com.faltenreich.diaguard.data.PreferenceHelper;
import com.faltenreich.diaguard.data.entity.Measurement;
import com.faltenreich.diaguard.util.Helper;

import butterknife.BindView;

/**
 * Created by Faltenreich on 17.10.2015.
 */
public class CategoryValueViewHolder extends BaseViewHolder<ListItemCategoryValue> {

    @BindView(R.id.category_value_1) TextView textViewOne;
    @BindView(R.id.category_value_2) TextView textViewTwo;

    public CategoryValueViewHolder(View view) {
        super(view);
    }

    @Override
    public void bindData() {
        ListItemCategoryValue listItem = getListItem();
        Measurement.Category category = listItem.getCategory();
        if (listItem.getValueOne() > 0) {
            float value = PreferenceHelper.getInstance().formatDefaultToCustomUnit(category, listItem.getValueOne());
            String valueForUi = Helper.parseFloat(value);
            textViewOne.setText(valueForUi);
        } else {
            textViewOne.setText(null);
        }
        if (listItem.getValueTwo() > 0) {
            textViewTwo.setVisibility(View.VISIBLE);
            float value = PreferenceHelper.getInstance().formatDefaultToCustomUnit(category, listItem.getValueTwo());
            String valueForUi = Helper.parseFloat(value);
            textViewTwo.setText(valueForUi);
        } else {
            textViewTwo.setVisibility(View.GONE);
            textViewTwo.setText(null);
        }
    }
}