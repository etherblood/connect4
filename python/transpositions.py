UPPER_BOUND, LOWER_BOUND, EXACT = range(3)

class TranspositionEntry:
	def __init__(self, id, type, score, depth):
		self.id = id
		self.type = type
		self.score = score
		self.depth = depth

class TranspositionTable:
	def __init__(self, size_base=20):
		self._size_base = size_base
		self._data = [None] * (1 << size_base)
		self._mask = len(self._data) - 1
		
		self._hits = 0
		self._misses = 0
		self._overwrites = 0
		self._stores = 0
		self._loads = 0

	def _index(self, id):
		#TODO: test collision rate
		hash = 11400714819323198485 * id
		return (hash >> (64 - self._size_base)) & self._mask
		
	def load(self, id):
		self._loads += 1
		index = self._index(id)
		entry = self._data[index]
		if entry and entry.id == id:
			self._hits += 1
			return entry
		self._misses += 1
		return None
	
	def store_raw(self, id, type, score, depth):
		self._stores += 1
		index = self._index(id)
		entry = self._data[index]
		if entry:
			self._overwrites += 1
			entry.id = id
			entry.type = type
			entry.score = score
			entry.depth = depth
		else:
			self._data[index] = TranspositionEntry(id, type, score, depth)
	
	def print_stats(self):
		print("TT-stats\n hits: " + str(self._hits) + "\n misses: " + str(self._misses) + "\n overwrites: " + str(self._overwrites) + "\n loads: " + str(self._loads) + "\n stores: " + str(self._stores))
