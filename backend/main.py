from flask import Flask, redirect, url_for, request, Response, jsonify
import requests as re, json, polyline
import model, math

app = Flask(__name__)

DIRECTION_URL_API = "https://maps.googleapis.com/maps/api/directions/json?"
GOOGLE_API_KEY = "AIzaSyC8glAUHZbPM1gzikYcGm-wQIX3PS6MMkU"


@app.route("/run")
def run():
	model.run()
	return "success"

@app.route("/getPaths", methods = ['GET'])
def getPaths():
	jsonRoutes = []
	if request.method == "GET":
		source_placeId = request.args['source_placeId']
		dest_placeId = request.args['dest_placeId']
		time = request.args['time'] 
		urlOrigin = "place_id:" + source_placeId
		urlDestination = "place_id:" + dest_placeId
		url = DIRECTION_URL_API + "origin=" + urlOrigin + "&destination=" + urlDestination + "&key=" + GOOGLE_API_KEY + "&alternatives=true" ;
		response = re.get(url)
		jsonObject = json.loads(response.text)
		jsonRoutes = jsonObject['routes']
		
		data = []
		for i in range(len(jsonRoutes)):
			jsonLeg = jsonRoutes[i]['legs']
			overview_polyline = jsonRoutes[i]['overview_polyline']
			#print(typeof(jsonLeg))
			distance = jsonLeg[0]['distance']['value']
			ETA = jsonLeg[0]['duration']['value']
			points = polyline.decode(overview_polyline['points'])
			print(points)
			y_predict = model.predict(points, time, distance, ETA)
			jsonRoutes[i]['AQI_predict'] = y_predict.tolist()
			data.append(jsonRoutes[i])

	return jsonify(data) 
			
		

if __name__ == "__main__":
	app.run("0.0.0.0", 8080, debug = True)


