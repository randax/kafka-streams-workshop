const elasticsearch = require('elasticsearch')

// Instantiate an Elasticsearch client.
const elasticsearchUrl = process.env.ELASTICSEARCH_HOSTS || 'http://localhost:9200'
const client = new elasticsearch.Client({
	hosts: [elasticsearchUrl],
})

// require Express.
const express = require('express')

// Instantiate an instance of express and hold the value in a constant called app.
const app = express()

// Require the body-parser library. Will be used for parsing body requests.
const bodyParser = require('body-parser')

// Require the path library.
const path = require('path')

// Ping the client to be sure Elasticsearch is up.
client.ping(
	{
		requestTimeout: 30000,
	},
	function (error) {
		// At this point, Elasticsearch is down, please check your Elasticsearch service.
		if (error) {
			console.error('Elasticsearch cluster is down!')
		} else {
			console.log('Elasticsearch cluster is ready!')
		}
	}
)

// Use the bodyparser as a middleware.
app.use(bodyParser.json())

// Set port for the app to listen on.
app.set('port', process.env.PORT || 3001)

// Set path to serve static files.
app.use(express.static(path.join(__dirname, 'public')))

// Enable CORS.
app.use(function (req, res, next) {
	res.header('Access-Control-Allow-Origin', '*')
	res.header('Access-Control-Allow-Methods', 'PUT, GET, POST, DELETE, OPTIONS')
	res.header('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept')
	next()
})

// Define the /search route that should return Elasticsearch results.
app.get('/search', function (req, res) {
	// Declare the query object to search Elasticsearch.
	// Return only 200 results from the first result found.
	// Also match any data where the name is like the query string sent in.
	let body = {
		size: 200,
		from: 0,
		query: {
			match: {
				name: req.query['q'],
			},
		},
	}
	// Perform the actual search passing in the index, the search query, and the type.
	client
		.search({index: 'books', body: body, type: '_doc'})
		.then((results) => {
			res.send(results.hits.hits)
		})
		.catch((err) => {
			console.log(err)
			res.send([])
		})
})

// Listen on the specified port.
app.listen(app.get('port'), function () {
	console.log('Running: http://localhost:' + app.get('port'))
})
