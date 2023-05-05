package oth.shipeditor.data;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

/**
 * @author Ontheheavens
 * @since 05.05.2023
 */
public class HullData {

    public HullData(URI uri) {
        Hull hull = null;
        try {
            File file = new File(uri);
            ObjectMapper objectMapper = new ObjectMapper();
            hull = objectMapper.readValue(file, Hull.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (hull == null) return;
        System.out.println(Arrays.toString(hull.bounds));
        System.out.println(hull.hullName);
    }

}
