package article;

public class Article {
	private String url;
	private String time;
	private String title;
	private String category;
	private String description;
	private String content;
	private String[] key;
	private Trends[] trends;

	public Article() {
		//no args constructor
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void setDesciption(String description) {
		this.description = description;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setKey(String[] key) {
		this.key = key;
	}

	public void setTrends(Trends[] trends) {
		this.trends = trends;
	}
	
	public String getContent() {
		return content;
	}
	
	public String getDescription() {
		return description;
	}
}