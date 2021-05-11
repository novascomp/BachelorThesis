package me.novascomp.home.flat.uploader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FlatGenerator {

    private final int flatsCount;
    private final ObjectMapper objectMapper;
    private final FlatUploader flatUploader;

    public FlatGenerator(int flatsCount, ObjectMapper objectMapper) {
        this.flatsCount = flatsCount;
        this.objectMapper = objectMapper;
        this.flatUploader = generateFlats();
    }

    public String getJSON() {
        String string = null;
        try {
            string = objectMapper.writeValueAsString(flatUploader);
        } catch (JsonProcessingException ex) {
            Logger.getLogger(FlatGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return string;
    }

    public FlatUploader getFlatUploader() {
        return flatUploader;
    }

    private FlatUploader generateFlats() {
        FlatUploader flats = new FlatUploader();

        Random random = new Random();

        List<Integer> sizes = new ArrayList<>();
        List<String> commonShareSizes = new ArrayList<>();
        int sizesSum = 0;

        for (int i = 0; i < flatsCount; i++) {
            int size = random.nextInt(150) + 30;
            sizesSum += size;
            sizes.add(size);
        }

        for (int i = 0; i < flatsCount; i++) {
            commonShareSizes.add(String.valueOf(sizes.get(i)) + "/" + String.valueOf(sizesSum));
        }

        for (int i = 0; i < flatsCount; i++) {
            NVHomeFlat homeFlat = new NVHomeFlat();
            homeFlat.setIdentifier(String.valueOf(i + 1));
            homeFlat.setSize(String.valueOf(sizes.get(i)) + " m2");
            homeFlat.setCommonShareSize(String.valueOf(commonShareSizes.get(i)) + "");
            flats.getFlatsToUpload().add(homeFlat);
        }
        return flats;
    }
}
