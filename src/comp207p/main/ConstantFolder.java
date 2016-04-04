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
	
	boolean deletePrev = false;
	
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
				System.out.println(handle.getInstruction().getName());
				if(deletePrev == true){
					try {
						il.delete(handle.getPrev());
						deletePrev = false;
					} catch (TargetLostException e) {
						InstructionHandle[] targets = e.getTargets();
				         for(int i=0; i < targets.length; i++) {
				           InstructionTargeter[] targeters = targets[i].getTargeters();
				     
				           for(int j=0; j < targeters.length; j++)
				             targeters[j].updateTarget(targets[i], handle);
				       }
					}
				}
				il = handler(handle, il);
				
				maintainLocals(handle);
				maintainStack(handle, il);
			}
			
			ldcExterminator(il);
			il.setPositions(true);
			m.setMaxStack();
			m.setMaxLocals();
			gen.replaceMethod(method, m.getMethod());
		}
        
		gen.setConstantPool(cpgen);
		
		this.optimized = gen.getJavaClass();
	}
	
	private InstructionList do_add(InstructionHandle handle, InstructionList il, int type){
		
		int index;
		
		//Instruction prev1 = handle.getPrev().getInstruction();
		//Instruction prev2 = handle.getPrev().getPrev().getInstruction();
		Number ldc1 = stack.pop();
		Number ldc2 = stack.pop();
		
		if (type == 1) 
		{
			int result = (int)ldc1 + (int)ldc2;
			index = cpgen.addInteger(result);
			stack.push(result);
		} else if (type == 2) {
			long result = (long)ldc1 + (long)ldc2;
			index = cpgen.addLong(result);
			stack.push(result);
		} else if (type == 3) {
			float result = (float)ldc1 + (float)ldc2;
			index = cpgen.addFloat(result);
			stack.push(result);
		} else {
			double result = (double)ldc1 + (double)ldc2;
			index = cpgen.addDouble(result);
			stack.push(result);
			System.out.println(result);
		}
		
		LDC new_ldc = new LDC(index);
		il.insert(handle, new_ldc);
		System.out.println("inserted");
//		try 
//		{
//			//il.delete(prev1);
//			//il.delete(prev2);
//			il.delete(handle);
//		} catch (TargetLostException e) 
//		{
//			InstructionHandle[] targets = e.getTargets();
//	         for(int i=0; i < targets.length; i++) {
//	           InstructionTargeter[] targeters = targets[i].getTargeters();
//	     
//	           for(int j=0; j < targeters.length; j++)
//	             targeters[j].updateTarget(targets[i], handle.getNext());
//	       }
//		}
		return il;
	}
	
	private InstructionList do_mul(InstructionHandle handle, InstructionList il){
		//Instruction prev1 = handle.getPrev().getInstruction();
		//Instruction prev2 = handle.getPrev().getPrev().getInstruction();
		
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
			InstructionHandle[] targets = e.getTargets();
	         for(int i=0; i < targets.length; i++) {
	           InstructionTargeter[] targeters = targets[i].getTargeters();
	     
	           for(int j=0; j < targeters.length; j++)
	             targeters[j].updateTarget(targets[i], handle.getNext());
	       }
		}
		return il;
	}
	
	private InstructionList do_sub(InstructionHandle handle, InstructionList il){
		Instruction prev1 = handle.getPrev().getInstruction();
		Instruction prev2 = handle.getPrev().getPrev().getInstruction();

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
			InstructionHandle[] targets = e.getTargets();
	         for(int i=0; i < targets.length; i++) {
	           InstructionTargeter[] targeters = targets[i].getTargeters();
	     
	           for(int j=0; j < targeters.length; j++)
	             targeters[j].updateTarget(targets[i], handle.getNext());
	       }
		}
		return il;
	}
	
	private InstructionList do_div(InstructionHandle handle, InstructionList il){
		Instruction prev1 = handle.getPrev().getInstruction();
		Instruction prev2 = handle.getPrev().getPrev().getInstruction();

		Number ldc1 = stack.pop();
		Number ldc2 = stack.pop();
		float result = (int)ldc2 / (int)ldc1;
		int index = cpgen.addFloat(result);
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
			InstructionHandle[] targets = e.getTargets();
	         for(int i=0; i < targets.length; i++) {
	           InstructionTargeter[] targeters = targets[i].getTargeters();
	     
	           for(int j=0; j < targeters.length; j++)
	             targeters[j].updateTarget(targets[i], handle.getNext());
	       }
		}
		return il;
	}
	
	private InstructionList do_rem(InstructionHandle handle, InstructionList il){
		Instruction prev1 = handle.getPrev().getInstruction();
		Instruction prev2 = handle.getPrev().getPrev().getInstruction();

		Number ldc1 = stack.pop();
		Number ldc2 = stack.pop();
		int result = (int)ldc2 % (int)ldc1;
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
			InstructionHandle[] targets = e.getTargets();
	         for(int i=0; i < targets.length; i++) {
	           InstructionTargeter[] targeters = targets[i].getTargeters();
	     
	           for(int j=0; j < targeters.length; j++)
	             targeters[j].updateTarget(targets[i], handle.getNext());
	       }
		}
		return il;
	}
	
	private InstructionList do_neg(InstructionHandle handle, InstructionList il){
		Instruction prev1 = handle.getPrev().getInstruction();

		Number ldc1 = stack.pop();
		int result = -(int)ldc1;
		int index = cpgen.addInteger(result);
		stack.push(result);
		LDC new_ldc = new LDC(index);
		il.insert(handle, new_ldc);
		try 
		{
			//il.delete(prev1);
			il.delete(handle);
		} catch (TargetLostException e) 
		{
			InstructionHandle[] targets = e.getTargets();
	         for(int i=0; i < targets.length; i++) {
	           InstructionTargeter[] targeters = targets[i].getTargeters();
	     
	           for(int j=0; j < targeters.length; j++)
	             targeters[j].updateTarget(targets[i], handle.getNext());
	       }
		}
		return il;
	}
	
	private InstructionList do_i2d(InstructionHandle handle, InstructionList il){
		Integer in = stack.pop().intValue();
		Double d = in.doubleValue();
		stack.push(d);
		
		return il;
	}
	
	private InstructionList handler(InstructionHandle handle, InstructionList il) 
	{
		Instruction inst = handle.getInstruction();
		if (inst instanceof IADD) {
			return do_add(handle, il, 1);
		} else if (inst instanceof LADD) {
			return do_add(handle, il, 2);
		} else if (inst instanceof FADD) {
			return do_add(handle, il, 3);
		} else if (inst instanceof DADD) {
			return do_add(handle, il, 4);
		} else if (inst instanceof IMUL) {
			 			return do_mul(handle, il);
			 		} else if (inst instanceof LMUL) {
			 			return do_mul(handle, il);
			 		} else if (inst instanceof FMUL) {
			 			return do_mul(handle, il);
			 		} else if (inst instanceof DMUL) {
			 			return do_mul(handle, il);
			 		} else if (inst instanceof ISUB) {
			 			return do_sub(handle, il);
			 		} else if (inst instanceof LSUB) {
			 			return do_sub(handle, il);
			 		} else if (inst instanceof FSUB) {
			 			return do_sub(handle, il);
			 		} else if (inst instanceof DSUB) {
						return do_sub(handle, il);
			 		} else if (inst instanceof IDIV) {
			 			return do_div(handle, il);
			 		} else if (inst instanceof LDIV) {
			 			return do_div(handle, il);
			 		} else if (inst instanceof FDIV) {
			 			return do_div(handle, il);
			 		} else if (inst instanceof DDIV) {
			 			return do_div(handle, il);
			 		} else if (inst instanceof IREM) {
			 			return do_rem(handle, il);
			 		} else if (inst instanceof LREM) {
			 			return do_rem(handle, il);
			 		} else if (inst instanceof FREM) {
			 			return do_rem(handle, il);
			 		} else if (inst instanceof DREM) {
			 			return do_rem(handle, il);
			 		} else if (inst instanceof INEG) {
			 			return do_neg(handle, il);
			 		} else if (inst instanceof LNEG) {
			 			return do_neg(handle, il);
			 		} else if (inst instanceof FNEG) {
			 			return do_neg(handle, il);
			 		} else if (inst instanceof DNEG) {
			 			return do_neg(handle, il);
			  		}
		return il;
	}
	
	private void maintainStack(InstructionHandle handle, InstructionList il){
		if(handle.getInstruction() instanceof ICONST){
			stack.push(Character.getNumericValue(handle.getInstruction().toString().charAt(7)));
		} else if(handle.getInstruction() instanceof LDC){
			LDC val = (LDC)handle.getInstruction();
			Object obj = val.getValue(cpgen);
			if(obj instanceof Number){
				stack.push((Number)obj);
			}
		} else if(handle.getInstruction() instanceof LDC2_W){
			LDC2_W val = (LDC2_W)handle.getInstruction();
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
		} else {
			return;
		}
		deletePrev = true;
	}
	
	private void maintainLocals(InstructionHandle handle){
		if(handle.getInstruction() instanceof StoreInstruction){
			locals[Character.getNumericValue((handle.getInstruction().toString().charAt(7)))] = stack.pop();
			deletePrev = true;
		} else if(handle.getInstruction() instanceof LoadInstruction && !(handle.getInstruction() instanceof ALOAD)){
			stack.push(locals[Character.getNumericValue(handle.getInstruction().toString().charAt(6))]);
			deletePrev = true;
		}
	}
	
	private void ldcExterminator(InstructionList il) 
	{
	 		InstructionHandle handle = il.getStart(), end = il.getEnd();
	 		do {
	 			InstructionHandle previous = handle.getPrev();
	 			if (previous.getInstruction() instanceof LDC || previous.getInstruction() instanceof LDC2_W)
	 				try {
	 					il.delete(previous);
	 				} catch (TargetLostException e) {
	 					e.printStackTrace();
	 				}
	 			handle = handle.getNext();
	 		} while (handle != end); 
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