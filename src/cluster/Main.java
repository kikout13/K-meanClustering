package cluster;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentNavigableMap;

import org.mapdb.DB;
import org.mapdb.DBMaker;

public class Main {

	private static String path = "tintuc/thegioi/";

	public static void main(String[] args) throws IOException {
		// Configure and open database using builder pattern.
		// All options are available with code auto-completion.
		File dbFile = File.createTempFile("mapdb", "db");
		DB db = DBMaker.newFileDB(dbFile).closeOnJvmShutdown()
				.encryptionEnable("datasection").make();
		// open an collection, TreeMap has better performance than HashMap
		ConcurrentNavigableMap<Integer, Map<String, Double>> dTable = db
				.getTreeMap("DocDatabase");
		ConcurrentNavigableMap<String, Integer> wTable = db
				.getTreeMap("WordDatabase");

		// parse all the files in folder
		Parser parser = new Parser(path);
		parser.contentParser(db, dTable, wTable);

		ArrayList<Integer> documentsList = new ArrayList<>();
		documentsList = parser.getDocumentsList();

		KmeanClustering clustering = new KmeanClustering(documentsList, 7);
		ArrayList<Cluster> clustersList = clustering.run(dTable, wTable);

		db.close();
	}
}