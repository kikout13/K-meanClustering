package cluster;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentNavigableMap;
public class KmeanClustering {
	
	private ArrayList<Integer> list;
	private ArrayList<Cluster> clustersList = new ArrayList<>();
	private int k_mean;
	
	public KmeanClustering(ArrayList<Integer> list, int k_mean) {
		this.list = list;
		this.k_mean = k_mean;
	}
	
	public Double distance(Map<String, Double> map1, Map<String, Double> map2) {
		Map<String, Double> tempMap1 = map1;
		Map<String, Double> tempMap2 = map2;
		Double weight1 = 0.00, weight2 = 0.00; // Do dai vector
		Double dotProduct = 0.00; // Tich vo huong
		for (Map.Entry entry : tempMap1.entrySet()) {
			weight1 += (Double) entry.getValue() * (Double) entry.getValue();
		}
		for (Map.Entry entry : tempMap2.entrySet()) {
			weight2 += (Double) entry.getValue() * (Double) entry.getValue();
		}
		weight1 = Math.sqrt(weight1);
		weight2 = Math.sqrt(weight2);
		if (tempMap1.size() < tempMap2.size())
			// Bang 1 nho hon bang 2
			for (Map.Entry entry : tempMap1.entrySet()) {
				if (tempMap2.containsKey(entry.getKey())) {
					dotProduct += (Double) entry.getValue()
							* (Double) tempMap2.get(entry.getKey());
				}
			}
		else
			// Bang 2 nho hon bang 1
			for (Map.Entry entry : tempMap2.entrySet())
				if (tempMap1.containsKey(entry.getKey())) {
					dotProduct += (Double) entry.getValue()
							* (Double) tempMap1.get(entry.getKey());
				}
		return dotProduct / (weight1 * weight2);
	}
	
	public Double distance(Integer dId1, Map<String, Double> centroid,
			ConcurrentNavigableMap<Integer, Map<String, Double>> dTable) {
		Map<String, Double> tempMap1 = dTable.get(dId1);
		return distance(tempMap1, centroid);
	}
	
	public Double distance(Integer dId1, Integer dId2,
			ConcurrentNavigableMap<Integer, Map<String, Double>> dTable) {
		Map<String, Double> tempMap = dTable.get(dId2);
		return distance(dId1, tempMap, dTable);
	}
	
	public ArrayList<Cluster> run(
			ConcurrentNavigableMap<Integer, Map<String, Double>> dTable,
			ConcurrentNavigableMap<String, Integer> wTable)
			throws FileNotFoundException {
		
		Map<String, Double> test1 = new HashMap<String, Double>();
		Map<String, Double> test2 = new HashMap<String, Double>();
		PrintWriter pw = new PrintWriter("log.txt");
		
		System.out.println("cluster init...");
		// khoi tao k cum, co tam la k observation dau tien trong list
		for (int i = 0; i < k_mean; i++) {
			Cluster cluster = new Cluster();
			cluster.setCentroid(dTable.get(list.get(i)));
			cluster.add(list.get(i));
			clustersList.add(cluster);
		}
		System.out.println();
		// chia cac observation con lai vao cac cum
		for (int i = k_mean; i < list.size(); i++) {
			double max_rss = distance(list.get(i), clustersList.get(0)
					.getCentroid(), dTable);
			int cluster_num = 0;
			for (int j = 1; j < clustersList.size(); j++) {
				double rss = distance(list.get(i), clustersList.get(j)
						.getCentroid(), dTable);
				if (max_rss < rss) {
					max_rss = rss;
					cluster_num = j;
				}
			}
			clustersList.get(cluster_num).add(list.get(i));
		}
		
		System.out.println("clustering...");
		
		// tinh lai tam cac cum, chia lai cac observation cho den khi khong co su di chuyen observation giua cac cum
		boolean flag = true;
		int count = 1;
		while (flag) {
			flag = false;
			for (Cluster cluster : clustersList) {
				Map<String, Double> new_centroid = new HashMap<String, Double>();
				ArrayList<Integer> documentsList = cluster.getDocumentsList();
				for (int i = 0; i < documentsList.size(); i++) {
					Map<String, Double> wordList = dTable.get(documentsList
							.get(i));
					for (Map.Entry entry : wordList.entrySet()) {
						if (!new_centroid.containsKey(entry.getKey())) {
							double sum = 0;
							sum += (Double) entry.getValue();
							for (int j = i + 1; j < documentsList.size(); j++) {
								if (dTable.get(documentsList.get(j))
										.containsKey(entry.getKey())) {
									sum += dTable.get(documentsList.get(j))
											.get(entry.getKey());
								}
							}
							new_centroid.put((String) entry.getKey(), sum
									/ cluster.size());
						}
					}
				}
				cluster.setCentroid(new_centroid);
			}
			// chia lai cac observation cho den khi khong co su dich chuyen observation giua cac cum
			for (int i = 0; i < list.size(); i++) {
				double max_rss = distance(list.get(i), clustersList.get(0)
						.getCentroid(), dTable);
				int cluster_num = 0;
				int contain_cluster = 0;
				for (int j = 1; j < clustersList.size(); j++) {
					double rss = distance(list.get(i), clustersList.get(j)
							.getCentroid(), dTable);
					if (max_rss < rss) {
						max_rss = rss;
						cluster_num = j;
					}
					if (clustersList.get(j).containts(list.get(i)))
						contain_cluster = j;
				}
				if (cluster_num != contain_cluster) {
					clustersList.get(contain_cluster).remove(list.get(i));
					clustersList.get(cluster_num).add(list.get(i));
					flag = true;
				}
			}
			System.out.println("Clustering times: " + (++count));
			pw.println("Clustering times: " + count);
			for (int i = 0; i < clustersList.size(); i++) {
				pw.println("Centroid " + i + ":"
						+ clustersList.get(i).getCentroid());
				System.out.println("Cluster " + (i + 1) + ": ");
				clustersList.get(i).print();
				System.out.println();
			}
		}
		pw.close();
		return clustersList;
	}
}