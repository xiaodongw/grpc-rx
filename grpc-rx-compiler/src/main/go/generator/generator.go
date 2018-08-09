package generator

import (
	"github.com/golang/protobuf/proto"
	"github.com/golang/protobuf/protoc-gen-go/descriptor"
	plugin "github.com/golang/protobuf/protoc-gen-go/plugin"
	"io/ioutil"
	"strconv"
	"strings"
)

// Generator is the type whose methods generate the output, stored in the associated response structure.
type Generator struct {
	Request  *plugin.CodeGeneratorRequest  // The input.
	Response *plugin.CodeGeneratorResponse // The output.

	Param       map[string]string // Command-line parameters.
	writeOutput bool
	writeInput  bool
}

// New creates a new generator and allocates the request and response protobufs.
func New() *Generator {
	g := new(Generator)
	g.Request = new(plugin.CodeGeneratorRequest)
	g.Response = new(plugin.CodeGeneratorResponse)
	return g
}

// CommandLineParameters breaks the comma-separated list of key=value pairs
// in the parameter (a member of the request protobuf) into a key/value map.
// It then sets file name mappings defined by those entries.
func (g *Generator) CommandLineParameters(parameter string) {
	g.Param = make(map[string]string)
	for _, p := range strings.Split(parameter, ",") {
		if i := strings.Index(p, "="); i < 0 {
			g.Param[p] = ""
		} else {
			g.Param[p[0:i]] = p[i+1:]
		}
	}

	for k, v := range g.Param {
		switch k {
		case "write_input":
			g.writeInput, _ = strconv.ParseBool(v)
		default:
			// unsupported parameter
		}
	}
}

func (g *Generator) WriteInput(data []byte) error {
	if !g.writeInput {
		return nil
	}

	filename := "request.bin"
	return ioutil.WriteFile(filename, data, 0644)
}

// GenerateAllFiles generates the output for all the files we're outputting.
func (g *Generator) GenerateAllFiles() error {
	// map of all files
	files := make(map[string]*descriptor.FileDescriptorProto)
	for _, file := range g.Request.ProtoFile {
		files[file.GetName()] = file
	}

	// collect all message types
	messages := make(map[string]Message)
	for _, file := range g.Request.ProtoFile {
		for _, msg := range file.MessageType {
			messages[file.GetPackage()+"."+msg.GetName()] = Message{
				Package:  file.GetPackage(),
				Name:     msg.GetName(),
				JavaName: g.generateJavaName(file, msg),
			}
		}
	}

	for _, fileName := range g.Request.FileToGenerate {
		file := files[fileName]
		for _, service := range file.Service {
			sg := ServiceGenerator{
				file:     file,
				service:  service,
				messages: &messages,
			}
			if err := sg.Generate(); err != nil {
				return err
			}

			fname := sg.GetFileName()
			g.Response.File = append(g.Response.File, &plugin.CodeGeneratorResponse_File{
				Name:    proto.String(fname),
				Content: proto.String(sg.String()),
			})
		}
	}

	return nil
}

func (g *Generator) generateJavaName(file *descriptor.FileDescriptorProto, msg *descriptor.DescriptorProto) string {
	if file.GetOptions().GetJavaMultipleFiles() {
		return file.GetOptions().GetJavaPackage() + "." + msg.GetName()
	} else {
		return file.GetOptions().GetJavaPackage() + "." + g.getOuterClass(file) + "." + msg.GetName()
	}
}
func (g *Generator) getOuterClass(file *descriptor.FileDescriptorProto) string {
	outer := file.GetOptions().GetJavaOuterClassname()
	if len(outer) != 0 {
		return outer
	} else {
		name := strings.Replace(file.GetName(), ".proto", "", -1)
		return strings.Title(name)
	}
}
