package com.hdgh0g.image_freg_analyzer;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDescriptor;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.style.Styler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Main {

    private static int BUCKET_WIDTH = 18;
    private static final String PATH = "C:\\Users\\krlva\\Dropbox\\RAW";

    public static void main(String[] args) throws ImageProcessingException, IOException {
        List<Integer> focals = getAllUsedFocals();
        Map<Integer, Long> distributionMap = constructDistributionMap(focals);
        List<Long> yData = new ArrayList<>(distributionMap.values());
        List<String> xData = distributionMap.keySet().stream()
                .map(k -> k + "-" + (k + BUCKET_WIDTH))
                .collect(Collectors.toList());
        CategoryChart chart = prepareChart(yData, xData);
        new SwingWrapper<>(chart).displayChart();
    }

    private static CategoryChart prepareChart(List<Long> yData, List<String> xData) {
        CategoryChart chart = new CategoryChartBuilder().width(800).height(600)
                .xAxisTitle("Focal Length")
                .yAxisTitle("Frequency")
                .build();

        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setAvailableSpaceFill(0.99);
        chart.getStyler().setOverlapped(true);
        chart.addSeries("focals", xData, yData);
        return chart;
    }

    private static Map<Integer, Long> constructDistributionMap(List<Integer> focals) {
        Map<Integer, Long> distributionMap = new TreeMap<>();

        long max = focals.stream().mapToInt(c -> c).max().orElseThrow(RuntimeException::new);
        for (int i = 0; i <= max; i += BUCKET_WIDTH) {
            distributionMap.put(i, 0L);
        }
        focals.forEach(f -> {
            int key = f / BUCKET_WIDTH * BUCKET_WIDTH;
            distributionMap.put(key, distributionMap.get(key) + 1);
        });
        return distributionMap;
    }

    private static List<Integer> getAllUsedFocals() throws ImageProcessingException, IOException {
        List<Integer> focals = new ArrayList<>();
        File dir = new File(PATH);
        for (File file : dir.listFiles()) {
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            ExifSubIFDDirectory exif = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            ExifSubIFDDescriptor descriptor = new ExifSubIFDDescriptor(exif);
            String focal = descriptor.getFocalLengthDescription();

            if (focal != null) {
                focals.add(Integer.parseInt(focal.replaceAll("\\D", "")));
            }
        }
        return focals;
    }

}
