package cluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Cluster {

	private Map<String, Double> centroid;
	private ArrayList<Integer> documentsList;

	public Cluster() {
		this.centroid = new HashMap<String, Double>();
		this.documentsList = new ArrayList<Integer>();
	}

	public void setCentroid(Map<String, Double> centroid) {
		this.centroid = centroid;
	}

	public Map<String, Double> getCentroid() {
		return centroid;
	}

	public ArrayList<Integer> getDocumentsList() {
		return this.documentsList;
	}

	public void setDocumentsList(ArrayList<Integer> list) {
		documentsList = list;
	}

	public int getDocument(int i) {
		return documentsList.get(i);
	}

	public boolean containts(Integer document) {
		return documentsList.contains(document);
	}

	public boolean add(Integer document) {
		return documentsList.add(document);
	}

	public boolean remove(Integer document) {
		if (documentsList.contains(document)) {
			return documentsList.remove(document);
		} else
			return false;
	}

	public int size() {
		return documentsList.size();
	}

	public void print() {
		for (int i = 0; i < documentsList.size(); i++) {
			System.out.print(String.format("%06d", (documentsList.get(i)+1)) + ".json    ");
		}
	}
}