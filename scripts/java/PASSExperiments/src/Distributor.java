import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Distributor {

    public ArrayList<File[]> distribute(String what, long threads, String filter) throws IOException {
        int number_threads = Math.toIntExact(threads);
        File[] total_configs = new File(what).listFiles((file, s) -> s.toLowerCase().endsWith(filter));
        if (total_configs.length < number_threads)
            number_threads = total_configs.length;
        int part_size = total_configs.length / number_threads;
        int last_part_size = part_size + (total_configs.length - part_size * number_threads);
        ArrayList<File[]> files = new ArrayList<>(number_threads);
        File[] part;
        for (int i = 0, start = 0; i < number_threads; i++, start += part_size) {
            if (i == number_threads - 1)
                part = slice(total_configs, start, start + last_part_size);
            else
                part = slice(total_configs, start, start + part_size);
            files.add(part);
        }
        return files;
    }

    private File[] slice(File[] input, int start, int end){
        File[] slicedArray = new File[end - start];
        for (int i = 0; i < slicedArray.length; i++)
        {
            slicedArray[i] = input[start + i];
        }
        return slicedArray;
    }

}
