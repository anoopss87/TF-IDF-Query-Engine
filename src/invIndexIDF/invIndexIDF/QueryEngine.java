package invIndexIDF.invIndexIDF;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class QueryEngine
{
	private static Map<String, ArrayList<String>> iIndexMap = new HashMap<String, ArrayList<String>>();
	private static Map<String, ArrayList<String>> iIndexPairMap = new HashMap<String, ArrayList<String>>();
	private static Map<String, String> urlMap = new HashMap<String,String>();	
	private static Map<String, String> pRankMap = new HashMap<String,String>();
	private static int max_result = 10;
	
	public static void buildPRankList(String filePath) throws IOException
	{
		BufferedReader br = null;
		try
		{
			String line;
			br = new BufferedReader(new FileReader(filePath + "\\pageRank.txt"));

			while ((line = br.readLine()) != null)
			{
				String words[] = line.split(" ");				
				pRankMap.put(words[0], words[1]);				
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			br.close();
		}
	}
	
	public static void buildUrlList(String filePath)throws IOException
	{
		BufferedReader br = null;
		try
		{
			String line;
			br = new BufferedReader(new FileReader(filePath + "\\urllist.txt"));

			while ((line = br.readLine()) != null)
			{
				String words[] = line.split(",");				
				urlMap.put(words[0], words[1]);				
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			br.close();
		}
	}
	public static void buildInvIndexTable(String filePath) throws IOException
	{
		BufferedReader br = null;
		try
		{
			String line;
			br = new BufferedReader(new FileReader(filePath + "\\iIndex.txt"));

			while ((line = br.readLine()) != null)
			{
				String words[] = line.split("\t");
				String res[] = words[1].split("`");
				ArrayList<String> temp = new ArrayList<String>();
				for(int i=0;i<res.length;++i)
				{
					if(i == 5)
						break;
					String[] entry = res[i].split(":");
					temp.add(entry[0]);					
				}
				iIndexMap.put(words[0], temp);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			br.close();
		}
	}
	
	public static void buildInvIndexPairTable(String filePath) throws IOException
	{
		BufferedReader br = null;
		try
		{
			String line;
			br = new BufferedReader(new FileReader(filePath + "\\iIndexPair.txt"));

			while ((line = br.readLine()) != null)
			{
				String words[] = line.split("\t");
				String res[] = words[1].split("`");
				ArrayList<String> temp = new ArrayList<String>();
				for(int i=0;i<res.length;++i)
				{
					if(i == 5)
						break;
					String[] entry = res[i].split(":");
					temp.add(entry[0]);					
				}
				iIndexPairMap.put(words[0], temp);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			br.close();
		}
	}
	
	public static LinkedHashSet<String> intersect(LinkedHashSet<String> h1, LinkedHashSet<String> h2)
	{
		LinkedHashSet<String> res = new LinkedHashSet<String>();
		for(String s : h1)
		{
			if(h2.contains(s))
				res.add(s);
		}
		return res;
	}
	
	public static LinkedHashSet<String> union(LinkedHashSet<String> h1, LinkedHashSet<String> h2)
	{
		LinkedHashSet<String> res = new LinkedHashSet<String>();
		res.addAll(h1);
		for(String s : h2)
		{			
			res.add(s);
		}
		return res;
	}
	
	public static LinkedHashSet<String> minus(LinkedHashSet<String> left, LinkedHashSet<String> right)
	{		
		for(String s : right)
		{
			if(left.contains(s))
				left.remove(s);
		}		
		return left;
	}
	
	private static LinkedHashSet<String> sortMap(LinkedHashSet<String> val)
	{
		SortedMap<Double, String> myMap = new TreeMap<Double, String>(Collections.reverseOrder());		
		LinkedHashSet<String> res = new LinkedHashSet<String>();
		for(String s : val)
		{
			myMap.put(Double.parseDouble(pRankMap.get(s)), s);				
		}
		for(Double d : myMap.keySet())
		{
			if(res.size() == max_result)
				break;
			res.add(myMap.get(d));				
		}
		return res;
	}
	
	public static LinkedHashSet<String> pairResult(String searchLine)
	{
		String[] str = searchLine.split(" ");
		LinkedHashSet<String> pairList = new LinkedHashSet<String>();		
		
		for(int i=0;i<str.length;++i)
		{
			for(int j=i+1;j<str.length;++j)
			{
				String pair = str[i] + str[j];
				ArrayList<String> temp = iIndexPairMap.get(pair);
				if(temp != null)
				{
					for(String urlId : temp)
					{
						if(!urlId.isEmpty())
							pairList.add(urlMap.get(urlId));
					}
				}
				else
				{
					pair = str[j] + str[i];
					temp = iIndexPairMap.get(pair);
					if(temp != null)
					{
						for(String urlId : temp)
						{
							if(!urlId.isEmpty())
								pairList.add(urlMap.get(urlId));
						}
					}
				}
			}
		}
		LinkedHashSet<String> res = new LinkedHashSet<String>();
		res = sortMap(pairList);
		return res;
	}
	
	private static LinkedHashSet<String> unionResult(String searchLine)
	{
		String[] str = searchLine.split(" ");
		
		LinkedHashSet<String> resUnion = new LinkedHashSet<String>();
		LinkedHashSet<String> resInter = new LinkedHashSet<String>();		
		LinkedHashSet<String> res = new LinkedHashSet<String>();
		
		int count = 0;
			
		for(String search : str)
		{
			count++;						
			LinkedHashSet<String> temp = new LinkedHashSet<String>();			
			
			if(iIndexMap.containsKey(search))
			{						
				for(String s : iIndexMap.get(search))
				{														
					temp.add(s);							
				}											
			}
			
			//find union of multiple words results
			if(resUnion.isEmpty())
			{
				resUnion.addAll(temp);
			}
			else
			{
				resUnion = union(resUnion, temp);				
			}
			
			//find intersection of multiple words results
			if(count == 1)
			{
				resInter.addAll(temp);
			}
			else
			{				
				resInter = intersect(resInter, temp);
			}
		}
		
		resUnion = union(resUnion, resInter);
				
		if(resUnion.isEmpty())
		{
			System.out.println("No results found for the query \"" + searchLine + "\"");
		}
		else
		{
			LinkedHashSet<String> tmp = new LinkedHashSet<String>();
			for(String urlId : resUnion)
			{
				tmp.add(urlMap.get(urlId));
			}
			res = sortMap(tmp);			
		}
		return res;
	}
	
	private static LinkedHashSet<String> handleQuery(String line, boolean boost)
	{
		LinkedHashSet<String> result = new LinkedHashSet<String>();
		String[] str = line.split(" ");
		
		if(str.length >=2 && boost)
			result = pairResult(line);
		
		if(result.isEmpty() || result.size() < max_result)
		{
			LinkedHashSet<String> temp = unionResult(line);
			for(String url : temp)
			{
				result.add(url);
				if(result.size() == max_result)
					break;
			}
		}		
		return result;
	}
	
	public static void display(LinkedHashSet<String> val)
	{
		for(String s : val)
			System.out.println(s);
	}
	
	public static void main(String[] args) throws IOException
	{
		String filePath = System.getProperty("user.dir");
		buildInvIndexTable(filePath);
		buildInvIndexPairTable(filePath);
		buildUrlList(filePath);
		buildPRankList(filePath);
		try
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));				
						
			while(true)
			{
				System.out.println("Enter the query :");
				String searchLine = br.readLine();
				
				if(searchLine.equals("exit!"))
				{
					System.out.println("Exiting!!!!!!!!");
					System.exit(0);
				}
				LinkedHashSet<String> res = new LinkedHashSet<String>();
				
				//AND operator
				if(searchLine.contains("+"))
				{
					String[] splitLine = searchLine.split("\\+");
					
					for(int i=0;i<splitLine.length;++i)
					{
						if(i == 0)
						{
							res = handleQuery(splitLine[i], true);							
						}
						else
						{
							LinkedHashSet<String> temp = handleQuery(splitLine[i], true);							
							res = intersect(temp, res);
							display(res);
						}
					}
				}
				
				//NOT operator
				else if(searchLine.contains("-"))
				{
					String[] splitLine = searchLine.split("-");
					
					for(int i=0;i<splitLine.length;++i)
					{
						if(i == 0)
						{
							res = handleQuery(splitLine[i], true);							
						}
						else
						{
							LinkedHashSet<String> temp = handleQuery(splitLine[i], true);							
							res = minus(res, temp);
							display(res);
						}
					}
				}
				
				//OR operator
				else
				{
					res = handleQuery(searchLine, true);
					display(res);
				}
			}				
		}
		catch(IOException io)
		{
			io.printStackTrace();
		}		
	}	
}
