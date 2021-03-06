def to_mask(index):
	return to_flag(index) - 1

def to_flag(index):
	return 1 << index
	
def lowest_bit(i):
	return i & -i

def highest_bit_index(i):
	return i.bit_length() - 1
	
def highest_bit(i):
	return to_flag(highest_bit_index(i))

def bit_count(i):
    return bin(i).count("1")
	
def sparse_bit_count(i):
	count = 0
	while(i):
		i &= i - 1
		count += 1
	return count
