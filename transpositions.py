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

	def _index(self, id):
		#TODO: test collision rate
		hash = 11400714819323198485 * id
		return (hash >> (64 - self._size_base)) & self._mask
		
	def load(self, id):
		index = self._index(id)
		entry = self._data[index]
		if entry and entry.id == id:
			return entry
		return None
	
	def store_raw(self, id, type, score, depth):
		index = self._index(id)
		entry = self._data[index]
		if entry:
			entry.id = id
			entry.type = type
			entry.score = score
			entry.depth = depth
		else:
			self._data[index] = TranspositionEntry(id, type, score, depth)

	def store(self, entry):
		index = self._index(entry.id)
		self._data[index] = entry
