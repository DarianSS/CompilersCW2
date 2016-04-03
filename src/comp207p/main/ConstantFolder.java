package comp207p.main;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;

import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import org.apache.bcel.Repository;
import org.apache.bcel.util.InstructionFinder;
import org.apache.bcel.verifier.structurals.OperandStack;


public class ConstantFolder
{
	ClassParser parser = null;
	ClassGen gen = null;
	
	ConstantPoolGen cpgen = null;
	Deque<Number> stack;
	Number[] locals;

	JavaClass original = null;
	JavaClass optimized = null;

	public ConstantFolder(String classFilePath)
	{
		try{
			this.parser = new ClassParser(classFilePath);
			this.original = this.parser.parse();
			this.gen = new ClassGen(this.original);
			this.cpgen = gen.getConstantPool();
		} catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void optimize()
	{
		// Implement your optimization here
		
		Boolean optimised = false;
		
		Method[] methods = gen.getMethods();
		for (Method method : methods)
		{	
			Code methodCode = method.getCode();
			InstructionList il = new InstructionList(methodCode.getCode());
			MethodGen m = new MethodGen(method, gen.getClassName(), cpgen);
			il = m.getInstructionList();
			stack = new ArrayDeque<Number>();
			locals = new Number[6];
			
			for (InstructionHandle handle : il.getInstructionHandles()) 
			{
				
				il = handler(handle, il);
				maintainStack(handle);
				maintainLocals(handle);
				
//						/*||handle.getInstruction() instanceof DADD
//				 || handle.getInstruction() instanceof FADD
//				 || handle.getInstruction() instanceof LADD*/)
			}
			
			il.setPositions(true);
			m.setMaxStack();
			m.setMaxLocals();
			gen.replaceMethod(method, m.getMethod());
		}
        
		gen.setConstantPool(cpgen);
		
		this.optimized = gen.getJavaClass();
	}
	
	private InstructionList do_iadd(InstructionHandle handle, InstructionList il){
		//Instruction prev1 = handle.getPrev().getInstruction();
		//Instruction prev2 = handle.getPrev().getPrev().getInstruction();
		
		//if (prev1 instanceof LDC && prev2 instanceof LDC)
		//{
			Number ldc1 = stack.pop();
			Number ldc2 = stack.pop();
			int result = (int)ldc1 + (int)ldc2;
			int index = cpgen.addInteger(result);
			stack.push(result);
			LDC new_ldc = new LDC(index);
			il.insert(handle, new_ldc);
			try 
			{
				//il.delete(prev1);
				//il.delete(prev2);
				il.delete(handle);
			} catch (TargetLostException e) 
			{
				e.printStackTrace();
			}
		//}
		return il;
	}
	
	private InstructionList do_imul(InstructionHandle handle, InstructionList il){
		//Instruction prev1 = handle.getPrev().getInstruction();
		//Instruction prev2 = handle.getPrev().getPrev().getInstruction();
		
		//if (prev1 instanceof LDC && prev2 instanceof LDC)
		//{
			Number ldc1 = stack.pop();
			Number ldc2 = stack.pop();
			int result = (int)ldc1 * (int)ldc2;
			int index = cpgen.addInteger(result);
			stack.push(result);
			LDC new_ldc = new LDC(index);
			il.insert(handle, new_ldc);
			try 
			{
				//il.delete(prev1);
				//il.delete(prev2);
				il.delete(handle);
			} catch (TargetLostException e) 
			{
				e.printStackTrace();
			}
		//}
		return il;
	}
	
	private InstructionList do_isub(InstructionHandle handle, InstructionList il){
		//Instruction prev1 = handle.getPrev().getInstruction();
		//Instruction prev2 = handle.getPrev().getPrev().getInstruction();
		
		//if (prev1 instanceof LDC && prev2 instanceof LDC)
		//{
			Number ldc1 = stack.pop();
			Number ldc2 = stack.pop();
			int result = (int)ldc2 - (int)ldc1;
			int index = cpgen.addInteger(result);
			stack.push(result);
			LDC new_ldc = new LDC(index);
			il.insert(handle, new_ldc);
			try 
			{
				//il.delete(prev1);
				//il.delete(prev2);
				il.delete(handle);
			} catch (TargetLostException e) 
			{
				e.printStackTrace();
			}
		//}
		return il;
	}
	
	private InstructionList handler(InstructionHandle handle, InstructionList il){
		if(handle.getInstruction() instanceof IADD){
			return do_iadd(handle, il);
		} else if(handle.getInstruction() instanceof IMUL){
			return do_imul(handle, il);
		} else if(handle.getInstruction() instanceof ISUB){
			return do_isub(handle, il);
		}
		return il;
	}
	
	private void maintainStack(InstructionHandle handle){
		if(handle.getInstruction() instanceof ICONST){
			stack.push(Character.getNumericValue(handle.getInstruction().toString().charAt(7)));
		} else if(handle.getInstruction() instanceof LDC){
			LDC val = (LDC)handle.getInstruction();
			Object obj = val.getValue(cpgen);
			if(obj instanceof Number){
				stack.push((Number)obj);
			}
		} else if(handle.getInstruction() instanceof BIPUSH){
			BIPUSH val = (BIPUSH)handle.getInstruction();
			Number obj = val.getValue();
			stack.push(obj);
		} else if(handle.getInstruction() instanceof SIPUSH){
			SIPUSH val = (SIPUSH)handle.getInstruction();
			Number obj = val.getValue();
			stack.push(obj);
		}
	}
	
	private void maintainLocals(InstructionHandle handle){
		if(handle.getInstruction() instanceof ISTORE){
			locals[Character.getNumericValue((handle.getInstruction().toString().charAt(7)))] = stack.pop();
		} else if(handle.getInstruction() instanceof ILOAD){
			stack.push(locals[Character.getNumericValue(handle.getInstruction().toString().charAt(6))]);
		}
	}
	
	public void write(String optimisedFilePath)
	{
		this.optimize();

		try {
			FileOutputStream out = new FileOutputStream(new File(optimisedFilePath));
			this.optimized.dump(out);
		} catch (FileNotFoundException e) {
			// Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
	}
}