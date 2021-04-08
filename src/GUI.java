import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.json.simple.parser.ParseException;

public class GUI {
	
	JFrame window;
	JTextField start, end;
	JButton runSearch;
	
	WikiCrawler crawler;
	double totalTime = 0;
	JLabel currentPage, pagesProcessed, currentDepth, averageNumLinks, percentageDuplicate, prevPageTime, totalTimeLabel;
	JLabel[] pathSteps;
	
	public GUI() {
		
		crawler = null;
		
		window = new JFrame();
		window.setSize(600, 400);
		
		JLabel startPageLabel = new JLabel("Start Page");
		startPageLabel.setBounds(25, 20, 150, 20);
		
		JLabel endPageLabel = new JLabel("End Page");
		endPageLabel.setBounds(25, 70, 150, 20);
		
		start = new JTextField();
		start.setText("");
		start.setBounds(20, 40, 150, 20);
		
		end = new JTextField();
		end.setText("");
		end.setBounds(20, 90, 150, 20);
		
		runSearch = new JButton("Search");
		runSearch.setBounds(45, 140, 100, 20);
		
		JLabel pathOutputLabel = new JLabel("Path:");
		pathOutputLabel.setBounds(20, 170, 150, 20);
		
		pathSteps = new JLabel[7];
		for(int i = 0; i < pathSteps.length; i++) {
			pathSteps[i] = new JLabel("");
			pathSteps[i].setBounds(30, 190 + 20 * i, 1000, 20);
			window.add(pathSteps[i]);
		}
		
		currentPage = new JLabel("Current Page:");
		currentPage.setBounds(200, 40, 300, 20);
		
		pagesProcessed = new JLabel("Pages Processed:");
		pagesProcessed.setBounds(200, 60, 300, 20);
		
		currentDepth = new JLabel("Current Depth:");
		currentDepth.setBounds(200, 80, 300, 20);
		
		averageNumLinks = new JLabel("Average #Links:");
		averageNumLinks.setBounds(200, 100, 300, 20);
		
		percentageDuplicate = new JLabel("% Duplicate Links:");
		percentageDuplicate.setBounds(200, 120, 300, 20);
		
		prevPageTime = new JLabel("Prev Page Time:");
		prevPageTime.setBounds(200, 140, 300, 20);
		
		totalTimeLabel = new JLabel("Total Time:");
		totalTimeLabel.setBounds(200, 160, 300, 20);
	
		
		runSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					boolean error = false;
					
					WikipediaLinksAPIData startLinks = WikiCrawler.linksOnPage(start.getText());
					if(startLinks.links.size() == 0) {
						startPageLabel.setText("Start Page [INVALID]");
						error = true;
					} else {
						startPageLabel.setText("Start Page");
					}
					
					WikipediaLinksAPIData endLinks = WikiCrawler.linksOnPage(end.getText());
					if(endLinks.links.size() == 0) {
						endPageLabel.setText("End Page [INVALID]");
						error = true;
					} else {
						endPageLabel.setText("End Page");
					}
					
					if(!error) {
						start.setText(startLinks.articleName);
						end.setText(endLinks.articleName);
						crawler = new WikiCrawler(start.getText(), end.getText());
						totalTime = 0;
					}
					
				} catch (IOException | ParseException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		window.setLayout(null);
		window.add(startPageLabel);
		window.add(endPageLabel);
		window.add(start);
		window.add(end);
		window.add(runSearch);
		window.add(pathOutputLabel);
		window.add(currentPage);
		window.add(pagesProcessed);
		window.add(currentDepth);
		window.add(averageNumLinks);
		window.add(prevPageTime);
		window.add(totalTimeLabel);
		window.add(percentageDuplicate);
		
		window.setVisible(true);
		window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		window.setResizable(false);
		window.setTitle("Wikipedia, the free encyclopedia");
	
		
		while(true) {
			System.out.print("");
			if(crawler != null) {
				if(!crawler.finished()) {
					try {
						crawler.step();
					} catch (IOException | ParseException e) {
						e.printStackTrace();
					}
					currentPage.setText("Current Page: " + crawler.currentPage + " [" + crawler.parent.get(crawler.currentPage) + "]");
					pagesProcessed.setText("Pages Processed: " + crawler.processed);
					currentDepth.setText("Current Depth: " + crawler.depth.get(crawler.currentPage));
					averageNumLinks.setText("Average #Links: " + crawler.totalLinks / crawler.processed);
					double stepTimeTaken = (double)System.currentTimeMillis() / 1000.0 - crawler.stepStartTime;
					prevPageTime.setText("Prev Page Time: " + Math.floor(100 * stepTimeTaken) / 100.0 + "s");
					totalTime += stepTimeTaken;
					totalTimeLabel.setText("Total Time: " + Math.floor(100 * totalTime) / 100.0 + "s");
					percentageDuplicate.setText("% Duplicate Links: " + Math.floor(100 * (double)crawler.totalDuplicateLinks / (double)crawler.totalLinks) + "%");
				} else {
					ArrayList<String> path = crawler.getPath(crawler.end);
					for(int i = 0; i < pathSteps.length; i++) {
						pathSteps[i].setText("");
					}
					for(int i = 0; i < Math.min(path.size(), pathSteps.length); i++) {
						pathSteps[i].setText(path.get(i));
					}
					crawler = null;
				}
			}
		}

		
	}
	
}
