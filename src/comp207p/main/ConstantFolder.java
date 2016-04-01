package comp207p.main;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import org.apache.bcel.Repository;
import org.apache.bcel.util.InstructionFinder;


public class ConstantFolder
{
	ClassParser parser = null;
	ClassGen gen = null;

	JavaClass original = null;
	JavaClass optimized = null;

	public ConstantFolder(String classFilePath)
	{
		try{
			this.parser = new ClassParser(classFilePath);
			this.original = this.parser.parse();
			this.gen = new ClassGen(this.original);
		} catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void optimize()
	{
		ClassGen cgen = new ClassGen(original);
		ConstantPoolGen cpgen = cgen.getConstantPool();

		// Implement your optimization here
		
		Boolean optimised = false;
		
		System.out.println("CONSTANTS:");
		for (int i=0; i<cpgen.getSize(); i++) 
		{
			System.out.println(cpgen.getConstant(i));
		}
		
		System.out.println("METHODS:");
		Method[] methods = cgen.getMethods();
		for (Method method : methods)
		{
			System.out.println(method);
			
			Code methodCode = method.getCode();
			InstructionList il = new InstructionList(methodCode.getCode());
			MethodGen m = new MethodGen(method.getAccessFlags(), method.getReturnType(), method.getArgumentTypes(), null, method.getName(), cgen.getClassName(), il, cpgen);
			il = m.getInstructionList();
			
			for (InstructionHandle handle : il.getInstructionHandles()) 
			{
				System.out.println(handle);
				
				if (handle.getInstruction() instanceof DADD
				 || handle.getInstruction() instanceof FADD
				 || handle.getInstruction() instanceof IADD
				 || handle.getInstruction() instanceof LADD) 
				{	
					Instruction prev1 = handle.getPrev().getInstruction();
					Instruction prev2 = handle.getPrev().getPrev().getInstruction();
					
					if (prev1 instanceof LDC && prev2 instanceof LDC) 
					{	
						System.out.println("Optimising an add operation.");
						optimised = true;
						LDC ldc1 = (LDC)prev1;
						LDC ldc2 = (LDC)prev2;
						double result = (int)ldc1.getValue(cpgen) + (int)ldc2.getValue(cpgen); // cast to double
						int index = cpgen.addDouble(result);
						LDC new_ldc = new LDC(index);
						il.insert(handle, new_ldc);
						try 
						{
							il.delete(prev1);
							il.delete(prev2);
							il.delete(handle);
						} catch (TargetLostException e) 
						{
							e.printStackTrace();
						}
						
						System.out.println(result);
					}
				}
			}
			
			il.setPositions(true);
			m.setMaxStack();
			m.setMaxLocals();
			Method newMethod = m.getMethod();
			cgen.replaceMethod(method, newMethod);
		}
		
		// Printing the new CP and methods
		if (optimised) 
		{
			System.out.println("\nOPTIMISED:\n");
			
			System.out.println("CONSTANTS:");
			for (int i=0; i<cpgen.getSize(); i++) 
			{
				System.out.println(cpgen.getConstant(i));
			}
			
			System.out.println("METHDODS:");
			{
				for (Method method : cgen.getMethods()) 
				{
					System.out.println(method);
					Code methodCode = method.getCode();
					InstructionList il = new InstructionList(methodCode.getCode());
					for (InstructionHandle handle : il.getInstructionHandles()) 
					{
						System.out.println(handle);
					}
				}
			}
		}
        
		this.optimized = gen.getJavaClass();
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