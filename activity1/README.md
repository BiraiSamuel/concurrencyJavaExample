# Assignment 4 Activity 1
## Description
The initail Performer code only has one function for adding strings to an array: 

## Protocol

### Requests
request: { "selected": <int: 1=add, 2=remove, 3=display, 4=count, 5=reverse,
0=quit>, "data": <thing to send>}

  data <string>: add
  data <int> pop
  data <int> <int> switch but send as String

### Responses

sucess response: {"type": <"hello", add",
"remove", "display", "count", "switch", "quit"> "data": <thing to return> }

type <String>: echoes original selected from request
data <string>: add = new list, pop = new list, display = current list, count = num elements, switch = switch two elements


error response: {"type": "error", "message"": <error string> }
Should give good error message if something goes wrong


## How to run the program
### Terminal
Base Code, please use the following commands:
```
    For Server, run "gradle runServer -Pport=9099 -q --console=plain"
	
	For Task1, run "gradle runTask1 -Pport=9099 -q --console=plain"
	
	For Task2, run "gradle runTask2 -Pport=9099 -q --console=plain"
	
	For Task3, run "gradle runTask3 -Pport=9099 -q --console=plain --Pno=00"
```
```   
    For Client, run "gradle runClient -Phost=localhost -Pport=9099 -q --console=plain"
```   



