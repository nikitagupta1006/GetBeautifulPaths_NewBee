import pandas as pd
import requests
import math
from math import sin,cos
from sklearn.model_selection import train_test_split 
from sklearn.linear_model import LinearRegression
import pygeohash as gh
from sklearn.neighbors import KNeighborsClassifier 
from sklearn.preprocessing import MinMaxScaler


category_codes = {}

AQI_breakpoints = {
    "0-50": "Good",
    "51-100": "Satisfactory",
    "101-200": "Moderately Pollutated",
    "201-300": "Poor",
    "301-400": "Very Poor",
    "401-500": "Severe"
}

AQI_categories = {
    "Good": 0,
    "Satisfactory": 1,
    "Moderately Pollutated": 2,
    "Poor": 3,
    "Very Poor": 4,
    "Severe" : 5
}


pi = math.pi
x = pi / 720

def getAQIRow(pollutant,concentration):
    pollutant = str(pollutant)
    foundIdx = -1
    for i in range(len(AQI_idx_db)):
        if AQI_idx_db.iloc[i,0] == pollutant and AQI_idx_db.iloc[i,1] <= concentration and AQI_idx_db.iloc[i,2] >= concentration:
            foundIdx = i
            break
    return AQI_idx_db.iloc[foundIdx,:]

def calculateAQI(aqiIdxDBRow, conc):
    num = (aqiIdxDBRow['FinalAQI'] - aqiIdxDBRow['InitialAQI']) * (conc - aqiIdxDBRow['InitialConcentration'])
    den = aqiIdxDBRow['FinalConcentration'] - aqiIdxDBRow['InitialConcentration']
    return (num/den) + aqiIdxDBRow['InitialAQI']

# TODO: Try different methods' 
def getAQI(data):
    conc_CO = data['CO']
    conc_PM25 = data['PM2.5']
    aqiRow_CO = getAQIRow("CO", conc_CO) 
    aqiRow_PM25 = getAQIRow("PM2.5", conc_PM25)
    aqi_CO = calculateAQI(aqiRow_CO, conc_CO)
    aqi_PM25 = calculateAQI(aqiRow_PM25, conc_PM25)
    return integrateAQI(aqi_CO, aqi_PM25)

def integrateAQI(conc_1, conc_2):
    return (conc_1 + conc_2) / 2.0


def getSine(time):
    value = x * int(time)
    return math.sin(value)

def getCosine(time):
    value = x * int(time)
    return math.cos(value)

def classifyAQI(aqi):
    for key in AQI_breakpoints:
        aqirange = key.split("-")
        if aqi >= int(aqirange[0]) and aqi <= int(aqirange[1]):
            return AQI_breakpoints[key]
        

def getMinutes(time):
    hhmmss = time.split(" ")[1]
    temp = hhmmss.split(":")
    return int(temp[0]) * 60 + int(temp[1]) 

def encodeLatLng(row):
    return gh.encode(float(row['latitude']), float(row['longitude']), precision = 8)

def getAQIClass(aqi_level):
    return AQI_categories[aqi_level]


def run():
	sensor_data = pd.read_csv("/home/nikita/Downloads/r1.csv")
	filter_data = sensor_data.iloc[:, [12, 14, 15, 16, 24]]
	reduced_data = filter_data
	columns = ['CO', 'PM2.5', 'latitude', 'longitude', 'timeofday']
	reduced_data.columns = columns
	global AQI_idx_db 
	AQI_idx_db = pd.read_csv("/home/nikita/Downloads/aqi_db.csv")

	reduced_data['minutes'] = reduced_data['timeofday'].apply(getMinutes)
	reduced_data['AQI'] = round(reduced_data.apply(getAQI, axis = 1)) 
	reduced_data['sine_time'] = reduced_data['minutes'].apply(getSine)
	reduced_data['cosine_time'] = reduced_data['minutes'].apply(getCosine)
	reduced_data['geohash'] = reduced_data.apply(encodeLatLng, axis = 1)
	reduced_data['geohash'] = reduced_data.geohash.astype('category')

	global category_codes
	category_codes = dict(enumerate(reduced_data['geohash'].cat.categories))
	print("CATEGORY CODES: ")
	print(category_codes)
	reduced_data['encoded_geohash'] = reduced_data['geohash'].cat.codes 
	reduced_data['AQI_level'] = reduced_data['AQI'].apply(classifyAQI)
	reduced_data['output'] = reduced_data['AQI_level'].apply(getAQIClass)
	global scaler
	scaler = MinMaxScaler()
	scaler.fit(reduced_data[['encoded_geohash']])
	reduced_data['encoded_geohash'] = scaler.transform(reduced_data[['encoded_geohash']])


	x = reduced_data[['encoded_geohash', 'sine_time', 'cosine_time']]
	y = reduced_data['output']
	x_train, x_test, y_train, y_test = train_test_split(x, y, test_size = 0.25, shuffle = True)

	global knn
	knn = KNeighborsClassifier(n_neighbors = 9).fit(x_train, y_train)
	knn_predictions = knn.predict(x_test)
	print("test: ", knn.score(x_test, y_test))
	print("train: ", knn.score(x_train, y_train))


def predict(points, initialTime, distance, ETA): # called for each of the routes predicted

	time_per_unit_dist = ETA/ distance
	currentTime = int(initialTime.split(":")[0]) * 60  + int(initialTime.split(":")[1])
	input_data = []
	for i in range(len(points) - 1):
		slat = points[i][0]
		elat = points[i + 1][0]
		slon = points[i][1]
		elon = points[i + 1][1]
		dist_points = 6371.01 * math.acos(sin(slat) * sin(elat) + cos(slat) * cos(elat) * cos(slon - elon))
		geohash = gh.encode(points[i][0], points[i][1], precision = 8)
		encoded_geohash = "-1"
		for key,value in category_codes.items():
			if(value == geohash):
				encoded_geohash = key
		#encoded_geohash = category_codes[geohash]
		sine_time = getSine(currentTime)
		cosine_time = getCosine(currentTime)
		currentTime = currentTime + time_per_unit_dist * dist_points
		row = {}
		row['encoded_geohash'] = encoded_geohash 
		row['sine_time'] = sine_time
		row['cosine_time'] = cosine_time
		input_data.append(row)
	
	x_test = pd.DataFrame(input_data)
	print(input_data)
	y_test = knn.predict(x_test)
	return y_test	




