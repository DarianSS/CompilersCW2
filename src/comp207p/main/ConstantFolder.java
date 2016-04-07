package comp207p.main;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;


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
			this.gen = new ClassGen(this.original); gen.setMajor(50);
			this.cpgen = gen.getConstantPool();
		} catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void optimize()
	{
		// Implement your optimization here
		
		Method[] methods = gen.getMethods();
		for (Method method : methods)
		{
			Code methodCode = method.getCode();
			InstructionList il = new InstructionList(methodCode.getCode());
			MethodGen m = new MethodGen(method, gen.getClassName(), cpgen);
			il = m.getInstructionList();
			stack = new ArrayDeque<Number>();
			locals = new Number[15];
			
			for (InstructionHandle handle : il.getInstructionHandles()) 
			{
				//System.out.println(handle.getInstruction().getName()); //For debug
				il = handler(handle, il);
				maintainStack(handle, il);
				il = maintainLocals(handle, il);
				//System.out.println(il.toString()); //For debug
			}
			
			ldcExterminator(il);
			//System.out.println(il.toString()); //For debug
			il.setPositions(true);
			m.setMaxStack();
			m.setMaxLocals();
			gen.replaceMethod(method, m.getMethod());
		}
        
		gen.setConstantPool(cpgen);
		
		this.optimized = gen.getJavaClass();
	}

	private InstructionList removeHandle(InstructionList il, InstructionHandle handle) {
		try {
			il.delete(handle);
		} catch (TargetLostException e) {
			InstructionHandle[] targets = e.getTargets();
	         for(int i=0; i < targets.length; i++) {
	           InstructionTargeter[] targeters = targets[i].getTargeters();	     
	           for(int j=0; j < targeters.length; j++)
	             targeters[j].updateTarget(targets[i], handle.getNext());
	       }
		}
		return il;
	}
	
private InstructionList do_add(InstructionHandle handle, InstructionList il){
		
		int index;	
		Number ldc1 = stack.pop();
		Number ldc2 = stack.pop();
		char c = handle.getInstruction().getName().charAt(0); 
		if(ldc1 instanceof NullNumber || ldc2 instanceof NullNumber){
			stack.push(ldc2);
			stack.push(ldc1);
			return il;
		}
		
		switch (c) {
		case 'i' :
			int result = ldc2.intValue() + ldc1.intValue();
			index = cpgen.addInteger(result);
			il = insert(il, handle, new LDC(index));
			stack.push(result);
			break;
		case 'l' :
			long resultL = ldc2.longValue() + ldc1.longValue();
			index = cpgen.addLong(resultL);
			il = insert(il, handle, new LDC2_W(index));
			stack.push(resultL);
			break;
		case 'f' :
			float resultF = ldc2.floatValue() + ldc1.floatValue();
			index = cpgen.addFloat(resultF);
			il = insert(il, handle, new LDC(index));
			stack.push(resultF);
			break;
		case 'd' :
			double resultD = ldc2.doubleValue() + ldc1.doubleValue();
			index = cpgen.addDouble(resultD);
			il = insert(il, handle, new LDC2_W(index));
			stack.push(resultD);
			break;
		}
		
		return il;
	}
	
	private InstructionList do_mul(InstructionHandle handle, InstructionList il){
		
		int index;	
		Number ldc1 = stack.pop();
		Number ldc2 = stack.pop();
		char c = handle.getInstruction().getName().charAt(0); 
		if(ldc1 instanceof NullNumber || ldc2 instanceof NullNumber){
			stack.push(ldc2);
			stack.push(ldc1);
			return il;
		}
		
		switch (c) {
		case 'i' :
			int result = ldc2.intValue() * ldc1.intValue();
			index = cpgen.addInteger(result);
			il = insert(il, handle, new LDC(index));
			stack.push(result);
			break;
		case 'l' :
			long resultL = ldc2.longValue() * ldc1.longValue();
			index = cpgen.addLong(resultL);
			il = insert(il, handle, new LDC2_W(index));
			stack.push(resultL);
			break;
		case 'f' :
			float resultF = ldc2.floatValue() * ldc1.floatValue();
			index = cpgen.addFloat(resultF);
			il = insert(il, handle, new LDC(index));
			stack.push(resultF);
			break;
		case 'd' :
			double resultD = ldc2.doubleValue() * ldc1.doubleValue();
			index = cpgen.addDouble(resultD);
			il = insert(il, handle, new LDC2_W(index));
			stack.push(resultD);
			break;
		}
		
		return il;
	}

	private InstructionList do_sub(InstructionHandle handle, InstructionList il){
		
		int index;	
		Number ldc1 = stack.pop();
		Number ldc2 = stack.pop();
		char c = handle.getInstruction().getName().charAt(0); 
		if(ldc1 instanceof NullNumber || ldc2 instanceof NullNumber){
			stack.push(ldc2);
			stack.push(ldc1);
			return il;
		}
		
		switch (c) {
		case 'i' :
			int result = ldc2.intValue() - ldc1.intValue();
			index = cpgen.addInteger(result);
			il = insert(il, handle, new LDC(index));
			stack.push(result);
			break;
		case 'l' :
			long resultL = ldc2.longValue() - ldc1.longValue();
			index = cpgen.addLong(resultL);
			il = insert(il, handle, new LDC2_W(index));
			stack.push(resultL);
			break;
		case 'f' :
			float resultF = ldc2.floatValue() - ldc1.floatValue();
			index = cpgen.addFloat(resultF);
			il = insert(il, handle, new LDC(index));
			stack.push(resultF);
			break;
		case 'd' :
			double resultD = ldc2.doubleValue() - ldc1.doubleValue();
			index = cpgen.addDouble(resultD);
			il = insert(il, handle, new LDC2_W(index));
			stack.push(resultD);
			break;
		}

		return il;
	}
	
	private InstructionList do_div(InstructionHandle handle, InstructionList il){
		
		int index;	
		Number ldc1 = stack.pop();
		Number ldc2 = stack.pop();
		char c = handle.getInstruction().getName().charAt(0); 
		if(ldc1 instanceof NullNumber || ldc2 instanceof NullNumber){
			stack.push(ldc2);
			stack.push(ldc1);
			return il;
		}
		
		switch (c) {
		case 'i' :
			int result = ldc2.intValue() / ldc1.intValue();
			index = cpgen.addInteger(result);
			il = insert(il, handle, new LDC(index));
			stack.push(result);
			break;
		case 'l' :
			long resultL = ldc2.longValue() / ldc1.longValue();
			index = cpgen.addLong(resultL);
			il = insert(il, handle, new LDC2_W(index));
			stack.push(resultL);
			break;
		case 'f' :
			float resultF = ldc2.floatValue() / ldc1.floatValue();
			index = cpgen.addFloat(resultF);
			il = insert(il, handle, new LDC(index));
			stack.push(resultF);
			break;
		case 'd' :
			double resultD = ldc2.doubleValue() / ldc1.doubleValue();
			index = cpgen.addDouble(resultD);
			il = insert(il, handle, new LDC2_W(index));
			stack.push(resultD);
			break;
		}
		
		return il;
	}
	
	private InstructionList do_rem(InstructionHandle handle, InstructionList il){
		
		int index;	
		Number ldc1 = stack.pop();
		Number ldc2 = stack.pop();
		char c = handle.getInstruction().getName().charAt(0); 
		if(ldc1 instanceof NullNumber || ldc2 instanceof NullNumber){
			stack.push(ldc2);
			stack.push(ldc1);
			return il;
		}
		
		switch (c) {
		case 'i' :
			int result = ldc2.intValue() % ldc1.intValue();
			index = cpgen.addInteger(result);
			il = insert(il, handle, new LDC(index));
			stack.push(result);
			break;
		case 'l' :
			long resultL = ldc2.longValue() % ldc1.longValue();
			index = cpgen.addLong(resultL);
			il = insert(il, handle, new LDC2_W(index));
			stack.push(resultL);
			break;
		case 'f' :
			float resultF = ldc2.floatValue() % ldc1.floatValue();
			index = cpgen.addFloat(resultF);
			il = insert(il, handle, new LDC(index));
			stack.push(resultF);
			break;
		case 'd' :
			double resultD = ldc2.doubleValue() % ldc1.doubleValue();
			index = cpgen.addDouble(resultD);
			il = insert(il, handle, new LDC2_W(index));
			stack.push(resultD);
			break;
		}
		
		return il;
	}
	
	private InstructionList do_neg(InstructionHandle handle, InstructionList il){

		int index;
		Number ldc1 = stack.pop();
		char c = handle.getInstruction().getName().charAt(0); 
		if(ldc1 instanceof NullNumber){
			stack.push(ldc1);
			return il;
		}
		
		switch (c) {
		case 'i' :
			int result = -ldc1.intValue();
			index = cpgen.addInteger(result);
			il = insert(il, handle, new LDC(index));
			stack.push(result);
		case 'l' :
			long resultL = -ldc1.longValue();
			index = cpgen.addLong(resultL);
			il = insert(il, handle, new LDC2_W(index));
			stack.push(resultL);
		case 'f' :
			float resultF = -ldc1.floatValue();
			index = cpgen.addFloat(resultF);
			il = insert(il, handle, new LDC(index));
			stack.push(resultF);
		case 'd' :
			double resultD = -ldc1.doubleValue();
			index = cpgen.addDouble(resultD);
			il = insert(il, handle, new LDC2_W(index));
			stack.push(resultD);
		}
		
		return il;
	}
	
	private InstructionList do_convert(InstructionHandle handle, InstructionList il){
		char c = handle.getInstruction().toString().charAt(2);
		switch(c){
		case 'd': stack.push(stack.pop().doubleValue());
		break;
		case 'i': stack.push(stack.pop().intValue());
		break;
		case 'f': stack.push(stack.pop().floatValue());
		break;
		case 'l': stack.push(stack.pop().longValue());
		break;
		default: stack.push(stack.pop().intValue());
		break;
		}
		
		il = removeHandle(il, handle);
		
		return il;
	}
	
	private InstructionList handler(InstructionHandle handle, InstructionList il) 
	{
		Instruction inst = handle.getInstruction();
		
		if (inst instanceof IADD || inst instanceof LADD || inst instanceof FADD || inst instanceof DADD) {
			return do_add(handle, il);
		} else if (inst instanceof IMUL || inst instanceof LMUL || inst instanceof FMUL || inst instanceof DMUL) {
			return do_mul(handle, il);
		} else if (inst instanceof ISUB || inst instanceof LSUB || inst instanceof FSUB || inst instanceof DSUB) {
			return do_sub(handle, il);
		} else if (inst instanceof IDIV || inst instanceof LDIV || inst instanceof FDIV || inst instanceof DDIV) {
			return do_div(handle, il);
		} else if (inst instanceof IREM || inst instanceof LREM || inst instanceof FREM || inst instanceof DREM) {
			return do_rem(handle, il);
		} else if (inst instanceof INEG || inst instanceof LNEG || inst instanceof FNEG || inst instanceof DNEG) {
			return do_neg(handle, il);
		} else if(inst instanceof ConversionInstruction){
			return do_convert(handle, il);
		}
		return il;
	}
	
	private InstructionList insert(InstructionList il, InstructionHandle old, Instruction to){
		if(everIINC(old, il)){
			return il;
		}
		InstructionHandle new_handle = il.insert(old, to);
		for(InstructionHandle handle : il.getInstructionHandles()){
			if(handle.getInstruction() instanceof BranchInstruction){
				BranchInstruction inst = (BranchInstruction)handle.getInstruction();
				if(inst.getTarget() == old){
					inst.setTarget(new_handle);
				}
			}
		}
		il = removeHandle(il, old);
		return il;
	}
	
	private boolean everIINC(InstructionHandle handletocheck, InstructionList il){
		if(!(handletocheck.getInstruction() instanceof LocalVariableInstruction)){
			return false;
		}
		for(InstructionHandle handle : il.getInstructionHandles()){
			if(handle == handletocheck){
				continue;
			}
			if(handle.getInstruction() instanceof IINC){
				IINC iinc = (IINC) handle.getInstruction();
				if(iinc.getIndex() == ((LocalVariableInstruction)handletocheck.getInstruction()).getIndex()){
					return true;
				}
			}
		}
		return false;
	}
	
	private void maintainStack(InstructionHandle handle, InstructionList il){
		Instruction inst = handle.getInstruction();
		if(inst instanceof ICONST){
			stack.push(Character.getNumericValue(handle.getInstruction().toString().charAt(7)));
			//il = removeHandle(il, handle);
		} else if(inst instanceof LDC){
			LDC val = (LDC)inst;
			Object obj = val.getValue(cpgen);
			if(obj instanceof Number){
				stack.push((Number)obj);
				//il = removeHandle(il, handle);
			}
		} else if(inst instanceof LDC2_W){
			LDC2_W val = (LDC2_W)inst;
			Object obj = val.getValue(cpgen);
			if(obj instanceof Number){
				stack.push((Number)obj);
				//il = removeHandle(il, handle);
			}
		} else if(inst instanceof BIPUSH){
			BIPUSH val = (BIPUSH)inst;
			Number obj = val.getValue();
			stack.push(obj);
			//il = removeHandle(il, handle);
		} else if(inst instanceof SIPUSH){
			SIPUSH val = (SIPUSH)inst;
			Number obj = val.getValue();
			stack.push(obj);
			//il = removeHandle(il, handle);
		} else {
			return;
		}
	}
	
	private InstructionList maintainLocals(InstructionHandle handle, InstructionList il){
		Instruction inst = handle.getInstruction();
		if(inst instanceof StoreInstruction){
			locals[((StoreInstruction) inst).getIndex()] = stack.pop();
			//il = removeHandle(il, handle);
		} else if(inst instanceof LoadInstruction && !(inst instanceof ALOAD)){
			if(everIINC(handle, il)){
				stack.push(new NullNumber());
				return il;
			}
			stack.push(locals[((LoadInstruction) inst).getIndex()]);
			if(stack.peek() instanceof Integer){
				int index = cpgen.addInteger(stack.peek().intValue());
				il = insert(il, handle, new LDC(index));
			} else if(stack.peek() instanceof Long){
				int index = cpgen.addLong(stack.peek().longValue());
				il = insert(il, handle, new LDC2_W(index));
			} else if(stack.peek() instanceof Double){
				int index = cpgen.addDouble(stack.peek().doubleValue());
				il = insert(il, handle, new LDC2_W(index));
			} else if(stack.peek() instanceof Float){
				int index = cpgen.addFloat(stack.peek().floatValue());
				il = insert(il, handle, new LDC(index));
			}
			//il = removeHandle(il, handle);
		}
		return il;
	}
	
	private void ldcExterminator(InstructionList il) 
	{
		InstructionHandle handle = il.getStart().getNext(), end = il.getEnd();
		do {
			InstructionHandle previous = handle.getPrev();
			InstructionHandle next = handle.getNext();
			if ((handle.getInstruction() instanceof LDC || handle.getInstruction() instanceof LDC2_W) &&
					(previous.getInstruction() instanceof LDC || previous.getInstruction() instanceof LDC2_W) &&
					!(next.getInstruction() instanceof IfInstruction) && 
					!(next.getInstruction() instanceof DCMPG) &&
					!(next.getInstruction() instanceof DCMPL) &&
					!(next.getInstruction() instanceof FCMPG) &&
					!(next.getInstruction() instanceof FCMPL) &&
					!(next.getInstruction() instanceof LCMP)) {

				il = removeHandle(il, previous);
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