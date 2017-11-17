import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Hashtable;

public class Indexer {
    public static Hashtable<String, Hashtable<String, Hashtable<String, Integer>>> hashIndexes = new Hashtable <String, Hashtable<String, Hashtable<String, Integer>>>();
    public static Hashtable<String, Hashtable<String, BTree>> btreeIndexes = new Hashtable<String, Hashtable<String, BTree>>();
    private static final String DEFAULT_SEPARATOR = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
    private String dataSchema; 		//Contiene la informacion del esquema a escanea
    private int countBlocks = 1;	//Contador de bloques
    private int blockSize = 10;			//Limite del tama�o de cada bloque
    private Scanner inputStream;

    public Indexer () {}

    public void indexTable(String tableName) {
        readSchema(tableName);
        btreeIndexes.put(tableName, new Hashtable<String, BTree>());
        hashIndexes.put(tableName, new Hashtable<String, Hashtable<String, Integer>>());
        String[] tempSchema = dataSchema.split(DEFAULT_SEPARATOR);
        for (int i = 0; i < tempSchema.length; i++) {
            String[] schemaCol = tempSchema[i].split("-");
            if(schemaCol.length == 3){
                addIndex(i, tableName, schemaCol[0], schemaCol[2]);
            }
        }
    }

    public Boolean columnIsIndexed (String tableName, String columnName) {
        return columnIsHashIndexed(tableName, columnName) || columnIsBTreeIndexed(tableName, columnName);
    }

    public Boolean columnIsBTreeIndexed (String tableName, String columnName) {
        return btreeIndexes.containsKey(tableName) && btreeIndexes.get(tableName).get(columnName) != null;
    }

    public Boolean columnIsHashIndexed (String tableName, String columnName) {
        return hashIndexes.containsKey(tableName) && hashIndexes.get(tableName).containsKey(columnName);
    }

    private void addIndex(int columnIndex, String tableName, String columnName, String methodName) {
        Hashtable<String, Integer> hashtable = new Hashtable<String, Integer>();
        BTree btree = new BTree();
        countBlocks = 1;
        inputStream = nextBlock(tableName);
        do {
            int countRows = 0;
            while (countRows < blockSize && inputStream.hasNext()) {
                String row = inputStream.nextLine();
                String[] tempRow = row.split(DEFAULT_SEPARATOR);
                String key = tempRow[columnIndex];
                Integer block = countBlocks - 1;
                if (methodName.equals("hash")) {
                    hashtable.put(key, block);
                } else {
                    btree.put(key, block);
                }
                countRows++;
            }
            inputStream = nextBlock(tableName);
        } while (inputStream != null);
        if (methodName.equals("hash")) {
            hashIndexes.get(tableName).put(columnName, hashtable);
        } else {
            btreeIndexes.get(tableName).put(columnName, btree);
        }
        this.close();
    }

    private void readSchema(String tableName){
        File file = new File("data/myDB/"+tableName+"/schema.txt"); //Se carga el esquema
        try {
            Scanner inputStream = new Scanner(file);
            while (inputStream.hasNext()) {
                dataSchema = inputStream.nextLine();
            }
            inputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private Scanner nextBlock(String tableName){
        File file = new File("data/myDB/"+tableName+"/"+countBlocks+".csv");
        try {
            inputStream = new Scanner(file);
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            //No se encuentran mas bloques
            return null;
        }
        countBlocks++;
        return inputStream;
    }

    public void close(){
        if ( inputStream != null ) {
            inputStream.close();
        }
    }
}
