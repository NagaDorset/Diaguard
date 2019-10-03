package com.faltenreich.diaguard.export.pdf;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.faltenreich.diaguard.R;
import com.faltenreich.diaguard.data.PreferenceHelper;
import com.faltenreich.diaguard.data.dao.EntryDao;
import com.faltenreich.diaguard.data.dao.EntryTagDao;
import com.faltenreich.diaguard.data.dao.FoodEatenDao;
import com.faltenreich.diaguard.data.dao.MeasurementDao;
import com.faltenreich.diaguard.data.entity.Entry;
import com.faltenreich.diaguard.data.entity.EntryTag;
import com.faltenreich.diaguard.data.entity.FoodEaten;
import com.faltenreich.diaguard.data.entity.Meal;
import com.faltenreich.diaguard.data.entity.Measurement;
import com.faltenreich.diaguard.ui.list.item.ListItemCategoryValue;
import com.faltenreich.diaguard.util.Helper;
import com.faltenreich.diaguard.util.StringUtils;
import com.pdfjet.Align;
import com.pdfjet.Border;
import com.pdfjet.Cell;
import com.pdfjet.Color;
import com.pdfjet.CoreFont;
import com.pdfjet.Font;
import com.pdfjet.PDF;
import com.pdfjet.Table;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Faltenreich on 19.10.2015.
 */
public class PdfTable extends Table {

    private static final String TAG = PdfTable.class.getSimpleName();
    private static final float LABEL_WIDTH = 120;
    private static final int HOURS_TO_SKIP = 2;

    private WeakReference<Context> contextReference;
    private PDF pdf;
    private PdfPage page;
    private DateTime day;
    private Measurement.Category[] categories;
    private boolean exportNotes;
    private boolean exportTags;
    private boolean exportFood;
    private boolean splitInsulin;

    private Font fontNormal;
    private Font fontBold;
    @ColorInt private int alternatingRowColor;
    @ColorInt private int hyperglycemiaColor;
    @ColorInt private int hypoglycemiaColor;

    private int rows;

    PdfTable(Context context, PDF pdf, PdfPage page, DateTime day, Measurement.Category[] categories, boolean exportNotes, boolean exportTags, boolean exportFood, boolean splitInsulin) {
        super();
        this.contextReference = new WeakReference<>(context);
        this.pdf = pdf;
        this.page = page;
        this.day = day;
        this.categories = categories;
        this.exportNotes = exportNotes;
        this.exportTags = exportTags;
        this.exportFood = exportFood;
        this.splitInsulin = splitInsulin;
        this.alternatingRowColor = ContextCompat.getColor(context, R.color.background_light_primary);
        this.hyperglycemiaColor = ContextCompat.getColor(context, R.color.red);
        this.hypoglycemiaColor = ContextCompat.getColor(context, R.color.blue);
        init();
    }

    private Context getContext() {
        return contextReference.get();
    }

    private void init() {
        try {
            fontNormal = new Font(pdf, CoreFont.HELVETICA);
            fontBold = new Font(pdf, CoreFont.HELVETICA_BOLD);
            setData(getData());
        } catch (Exception exception) {
            Log.e(TAG, exception.getMessage());
        }
    }

    private float getCellWidth() {
        return (page.getWidth() - LABEL_WIDTH) / (DateTimeConstants.HOURS_PER_DAY / 2);
    }

    public float getHeight() {
        float height = 0;
        for (int row = 0; row < rows; row++) {
            try {
                height += getRowAtIndex(row).get(0).getHeight();
            } catch (Exception exception) {
                Log.e(TAG, exception.getMessage());
            }
        }
        return height;
    }

    private List<List<Cell>> getData() {
        List<List<Cell>> data = new ArrayList<>();
        data.add(getRowForHeader());

        LinkedHashMap<Measurement.Category, ListItemCategoryValue[]> values = EntryDao.getInstance().getAverageDataTable(day, categories, HOURS_TO_SKIP);
        int row = 0;
        for (Measurement.Category category : values.keySet()) {
            ListItemCategoryValue[] items = values.get(category);
            String label = category.toLocalizedString(getContext());
            int backgroundColor = row % 2 == 0 ? alternatingRowColor : Color.white;
            switch (category) {
                case INSULIN:
                    if (splitInsulin) {
                        data.add(getRowForValues(items, 0, label + " " + getContext().getString(R.string.bolus), backgroundColor));
                        data.add(getRowForValues(items, 1, label + " " + getContext().getString(R.string.correction), backgroundColor));
                        data.add(getRowForValues(items, 2, label + " " + getContext().getString(R.string.basal), backgroundColor));
                    } else {
                        data.add(getRowForValues(items, -1, label, backgroundColor));
                    }
                    break;
                case PRESSURE:
                    data.add(getRowForValues(items, 0, label + " " + getContext().getString(R.string.systolic_acronym), backgroundColor));
                    data.add(getRowForValues(items, 1, label + " " + getContext().getString(R.string.diastolic_acronym), backgroundColor));
                    break;
                default:
                    data.add(getRowForValues(items, 0, label, backgroundColor));
                    break;
            }
            row++;
        }

        if (exportNotes || exportTags || exportFood) {
            List<PdfNote> notes = new ArrayList<>();
            for (Entry entry : EntryDao.getInstance().getEntriesOfDay(day)) {
                List<String> entryNotesAndTagsOfDay = new ArrayList<>();
                List<String> foodOfDay = new ArrayList<>();
                if (exportNotes && !StringUtils.isBlank(entry.getNote())) {
                    entryNotesAndTagsOfDay.add(entry.getNote());
                }
                if (exportTags) {
                    List<EntryTag> entryTags = EntryTagDao.getInstance().getAll(entry);
                    for (EntryTag entryTag : entryTags) {
                        entryNotesAndTagsOfDay.add(entryTag.getTag().getName());
                    }
                }
                if (exportFood) {
                    Meal meal = (Meal) MeasurementDao.getInstance(Meal.class).getMeasurement(entry);
                    if (meal != null) {
                        for (FoodEaten foodEaten : FoodEatenDao.getInstance().getAll(meal)) {
                            String foodNote = foodEaten.print();
                            if (foodNote != null) {
                                foodOfDay.add(foodNote);
                            }
                        }
                    }
                }
                boolean hasEntryNotesAndTags = !entryNotesAndTagsOfDay.isEmpty();
                boolean hasFood = !foodOfDay.isEmpty();
                boolean hasAny = hasEntryNotesAndTags || hasFood;
                if (hasAny) {
                    boolean hasBoth = hasEntryNotesAndTags && hasFood;
                    String notesOfDay = hasBoth ?
                            // Break line for succeeding food
                            TextUtils.join("\n", new String[] { getNotesAsString(entryNotesAndTagsOfDay), getNotesAsString(foodOfDay) }) :
                            hasEntryNotesAndTags ?
                                    getNotesAsString(entryNotesAndTagsOfDay) :
                                    getNotesAsString(foodOfDay);
                    notes.add(new PdfNote(entry.getDate(), notesOfDay));
                }
            }
            if (notes.size() > 0) {
                for (PdfNote note : notes) {
                    boolean isFirst = notes.indexOf(note) == 0;
                    boolean isLast = notes.indexOf(note) == notes.size() - 1;
                    data.add(getRowForNote(note, isFirst, isLast));
                }
            }
        }

        rows = data.size();
        return data;
    }

    private String getNotesAsString(List<String> notes) {
        return TextUtils.join(", ", notes);
    }

    private List<Cell> getRowForHeader() {
        List<Cell> cells = new ArrayList<>();

        String weekDay = DateTimeFormat.forPattern("E").print(day);
        String date = String.format("%s %s", weekDay, DateTimeFormat.forPattern("dd.MM").print(day));
        Cell cell = new Cell(fontBold, date);
        cell.setWidth(LABEL_WIDTH);
        cell.setNoBorders();
        cells.add(cell);

        for (int hour = 0; hour < DateTimeConstants.HOURS_PER_DAY; hour += HOURS_TO_SKIP) {
            cell = new Cell(fontNormal, Integer.toString(hour));
            cell.setWidth(getCellWidth());
            cell.setFgColor(Color.gray);
            cell.setTextAlignment(Align.CENTER);
            cell.setNoBorders();
            cells.add(cell);
        }

        return cells;
    }

    private List<Cell> getRowForValues(ListItemCategoryValue[] items, int valueIndex, String label, int backgroundColor) {
        List<Cell> cells = new ArrayList<>();

        Cell cell = new Cell(fontNormal, label);
        cell.setBgColor(backgroundColor);
        cell.setFgColor(Color.gray);
        cell.setWidth(LABEL_WIDTH);
        cell.setNoBorders();
        cells.add(cell);

        for (ListItemCategoryValue item : items) {
            Measurement.Category category = item.getCategory();
            float value = 0;
            switch (valueIndex) {
                case -1:
                    value = item.getValueTotal();
                    break;
                case 0:
                    value = item.getValueOne();
                    break;
                case 1:
                    value = item.getValueTwo();
                    break;
                case 2:
                    value = item.getValueThree();
                    break;
            }
            int textColor = Color.black;
            if (category == Measurement.Category.BLOODSUGAR && PreferenceHelper.getInstance().limitsAreHighlighted()) {
                if (value > PreferenceHelper.getInstance().getLimitHyperglycemia()) {
                    textColor = hyperglycemiaColor;
                } else if (value < PreferenceHelper.getInstance().getLimitHypoglycemia()) {
                    textColor = hypoglycemiaColor;
                }
            }
            float customValue = PreferenceHelper.getInstance().formatDefaultToCustomUnit(category, value);
            String text = customValue > 0 ? Helper.parseFloat(customValue) : "";
            cell = new Cell(fontNormal, text);
            cell.setBgColor(backgroundColor);
            cell.setFgColor(textColor);
            cell.setWidth(getCellWidth());
            cell.setTextAlignment(Align.CENTER);
            cell.setNoBorders();
            cells.add(cell);
        }
        return cells;
    }

    private List<Cell> getRowForLabel(@StringRes int labelResId) {
        List<Cell> cells = new ArrayList<>();
        Cell cell = new Cell(fontBold, getContext().getString(labelResId));
        cell.setWidth(page.getWidth());
        cell.setFgColor(Color.gray);
        cell.setNoBorders();
        cell.setBorder(Border.TOP, true);
        cells.add(cell);
        return cells;
    }

    private List<Cell> getRowForNote(PdfNote note, boolean isFirst, boolean isLast) {
        ArrayList<Cell> cells = new ArrayList<>();

        Cell cell = new Cell(fontNormal, Helper.getTimeFormat().print(note.getDateTime()));
        cell.setFgColor(Color.gray);
        cell.setWidth(LABEL_WIDTH);
        cell.setNoBorders();

        if (isFirst) {
            cell.setBorder(Border.TOP, true);
        }
        if (isLast) {
            cell.setBorder(Border.BOTTOM, true);
        }

        cells.add(cell);

        PdfMultilineCell multilineCell = new PdfMultilineCell(fontNormal, note.getNote(), 55);
        multilineCell.setFgColor(Color.gray);
        multilineCell.setWidth(page.getWidth() - LABEL_WIDTH);
        multilineCell.setNoBorders();

        if (isFirst) {
            multilineCell.setBorder(Border.TOP, true);
        }
        if (isLast) {
            multilineCell.setBorder(Border.BOTTOM, true);
        }

        cells.add(multilineCell);
        return cells;
    }
}