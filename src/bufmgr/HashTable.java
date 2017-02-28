package bufmgr;



import java.util.LinkedList;
import java.math.BigInteger;


//HashTable class. Took the method names from the Java HashMap class
public class HashTable {
	
	//size of the table
	private int size;
	
	//LinkedList to handle collisions
	private LinkedList<data>[] list;
	
	
	public HashTable(int size) {
		this.size = size;
		//used BigInteger because of convenient nextProbablePrime() method
		BigInteger i = new BigInteger(Integer.toString(this.size));
		int HTSIZE = i.nextProbablePrime().intValue();
		list = new LinkedList[HTSIZE];
		
	}
	
	//Hash function. Picked arbitrary values for A and B for the hash formula given in the handout
	public int hashFunction(int key) {
		return (5 * key + 2) % list.length;
	}
	
	public boolean containsKey(int key) {
		if (list[hashFunction(key)] != null) {
			int hash = hashFunction(key);
			if (list[hash].size() != 0) {
				for (int i = 0; i < list[hash].size(); i++) {
					if (list[hash].get(i).key == key) {
						return true;
					}
				}
				return false;
				
			}
			else {
				return false;
			}
			
		}
		else {
			return false;
		}
		
	}
	
	public Integer get(int key) {
		int hash = hashFunction(key);
		if (list[hash] != null) {
			for (int i = 0; i < list[hash].size(); i++) {
				if (list[hash].get(i).key == key) {
					return list[hash].get(i).value;
				}
			}
		}
		
		return null;
	}
	
	
	public void put(int key, int value) {
		
		if (containsKey(key) == false) {
			int hash = hashFunction(key);
			if (list[hash] == null) {
				list[hash] = new LinkedList<data>();
				
			}
			list[hash].add(new data(key, value));
			size++;
		}
	}
	
	//returns null if there is nothing to remove
	public Integer remove(int key) {
		int hash = hashFunction(key);
		size--;
		for (int i = 0; i < list[hash].size(); i++) {
			if (list[hash].get(i).key == key) {
				int value = list[hash].get(i).value;
				list[hash].remove(i);
				return value;
			}
		}
		
		return null;
	}
	
	public int size(){
		return size;
	}
	
	
	

}

//Basic class to store key-value pairs
class data {
	int key, value;
	public data(int key, int value) {
		this.key = key;
		this.value = value;
	}
	
	
}