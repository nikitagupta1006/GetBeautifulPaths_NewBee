# GetBeautifulPaths_NewBee
<h3>Objective:</h3>
Develop an application utilizing a robust routing algorithm, suggesting routes based on the following metrics: 
  <ol type = "1">
  <li>Mode of transportation of the commuter</li>
  <li>Air quality index</li>
  <li>Congestion levels</li>
  </ol>

<h3>Implementation</h3>
For a given source and destination (entered by the user):
<ol type = "1">
  <li>Evaluate all the alternative paths possible</li>
  <li>Calculating the AQI for latitudes and longitudes in a specific path, after certain regular intervals</li>
  <li>Evaluating the following parameters for every path:
    <ol type = "a">
     <li>AQI (using KNN classifier)</li>
      <li>ETA</li>
     <li>Estimated distance</li>
    </ol>
  </li>
  <li>Taking into account user's preference among the above stated parameters (to show the routes rearranged in 
    that specific manner) </li>
  <li>Display navigation instructions to the user</li>
  </ol>
