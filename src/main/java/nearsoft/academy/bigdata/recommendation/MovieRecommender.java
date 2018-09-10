package nearsoft.academy.bigdata.recommendation;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class MovieRecommender {

    List<String> productsTXT = new ArrayList<String>();
    List<String> reviewsTXT = new ArrayList<String>();
    BidiMap <String, Integer> usuarios = new DualHashBidiMap<String, Integer>();
    BidiMap <String, Integer> productos = new DualHashBidiMap<String, Integer>();

    String archivoCSV = "./src/main/resources/datos.csv";
    String producto = null;
    String userID = null;
    String score=null;
    int contador=0;

    public MovieRecommender(String file) throws Exception {
        readFile(file);
    }

    public void readFile(String file) throws Exception {

        PrintWriter pw;
        File archivo = new File(archivoCSV);

        if (archivo.exists()){
            archivo.delete();
        }
        pw = new PrintWriter(new File(archivoCSV));
        
        BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
        String content;

        //Haciendo el CSV
        while ((content = in.readLine()) != null) {

            StringBuilder stringBuilder = new StringBuilder();

            if (content.startsWith("product/productId")) {
                productsTXT.add(content.substring(19));
                producto = content.substring(19);
                if (!productos.containsKey(producto)){
                    productos.put(producto, productos.size());
                }
                contador++;
            }
            if (content.startsWith("review/userId")) {
                reviewsTXT.add(content.substring(15));
                userID=content.substring(15);
                if (!usuarios.containsKey(userID)) {
                    usuarios.put(userID, usuarios.size() + 1);
                }
                contador++;
            }
            if (content.startsWith("review/score")){
                score=content.substring(14,17);
                contador++;
            }
            if (contador==3) {
                //userID, productID, score
                stringBuilder.append(usuarios.get(userID));
                stringBuilder.append(',');
                stringBuilder.append(productos.get(producto));
                stringBuilder.append(',');
                stringBuilder.append(score);
                stringBuilder.append('\n');

                pw.write(stringBuilder.toString());
                contador=0;
            }
        }

        pw.close();
    }

    public int getTotalReviews() {

        return productsTXT.size();
    }

    public int getTotalProducts() {

        return productos.size();
    }

    public int getTotalUsers() {

        return usuarios.size();
    }

    public List<String> getRecommendationsForUser(String userId) throws Exception{

        DataModel model = new FileDataModel(new File(archivoCSV));
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

        List<RecommendedItem> recommendations = recommender.recommend(usuarios.get(userId), 3);
        List<String> resultados = new ArrayList<String>();

        BidiMap<Integer, String> rMap = productos.inverseBidiMap();

        for (RecommendedItem recommendation : recommendations) {
            String movieName = rMap.get((int) recommendation.getItemID());
            resultados.add(movieName);
        }

        return resultados;
    }
}