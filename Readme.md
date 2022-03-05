# On Ramp 8
Repo holds several examples of using gRPC. Branches are organized by
problem/topic and solution/topic. For example:
```
# checkout to create your own implementation following Test Driven Development
git checkout problem/call-types

# checkout to see a working solution that makes the test pass
git checkout solution/call-types
```


## Call Types
Run `call_types.sh` to print out the todos that summarize
the tasks that need to be completed to make the tests in
[TestGrpcCallTypes](src/test/java/com/baeldung/grpc/calls/TestGrpcCallTypes.java)
pass. The tasks are numbered, so start with `TODO: 1`, then do `TODO: 2`, then
`TODO: 3`, ect. Note the completed solution is on branch `solution/call-types`

Lastly this is code is based on code from [baeldung](https://www.baeldung.com/java-grpc-streaming)
