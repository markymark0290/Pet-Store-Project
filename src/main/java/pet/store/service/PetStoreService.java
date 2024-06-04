package pet.store.service;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pet.store.controller.model.PetStoreCustomer;
import pet.store.controller.model.PetStoreData;
import pet.store.controller.model.PetStoreEmployee;
import pet.store.dao.CustomerDao;
import pet.store.dao.EmployeeDao;
import pet.store.dao.PetStoreDao;
import pet.store.entity.Customer;
import pet.store.entity.Employee;
import pet.store.entity.PetStore;

@Service
public class PetStoreService {
	
	@Autowired
	private PetStoreDao petStoreDao;
	@Autowired
	private EmployeeDao employeeDao;
	@Autowired
	private CustomerDao customerDao;

	public PetStoreData savePetStore(PetStoreData petStoreData) {
		
		Long petStoreId = petStoreData.getPetStoreId();
		PetStore petStore = findOrCreatePetStore(petStoreId);
		copyPetStoreFields(petStore, petStoreData);
		PetStore dbPetStore = petStoreDao.save(petStore);
		
		
		return new PetStoreData(dbPetStore);
					
	}
	
	private void copyPetStoreFields(PetStore petStore, PetStoreData petStoreData) {
		petStore.setPetStoreAddress(petStoreData.getPetStoreAddress());
		petStore.setPetStoreId(petStoreData.getPetStoreId());
		petStore.setPetStoreCity(petStoreData.getPetStoreCity());
		petStore.setPetStoreName(petStoreData.getPetStoreName());
		petStore.setPetStorePhone(petStoreData.getPetStorePhone());
		petStore.setPetStoreState(petStoreData.getPetStoreState());
		petStore.setPetStoreZip(petStoreData.getPetStoreZip());
		
	}

	private PetStore findOrCreatePetStore(Long petStoreId) {
		PetStore petStore;
		
		if(Objects.isNull(petStoreId)) {
			petStore = new PetStore();
			
		}
		else {
			petStore = findPetStoreById(petStoreId);
		}
		
		return petStore;
	}

	private PetStore findPetStoreById(Long petStoreId) {
		
		return petStoreDao.findById(petStoreId).orElseThrow( 
				() -> new NoSuchElementException("Pet Store with Id=" + petStoreId + " does not exist."));
				
	}
	@Transactional(readOnly = false)
	public PetStoreEmployee saveEmployee(Long petStoreId, PetStoreEmployee petStoreEmployee) {
		
		PetStore petStore = findPetStoreById(petStoreId);
		Long employeeId = petStoreEmployee.getEmployeeId();
		Employee employee = findOrCreateEmployee(employeeId, petStoreId);
		
		copyEmployeeFields(employee, petStoreEmployee);
		
		employee.setPetStore(petStore);
		Set<Employee> employees = new HashSet<Employee>();
		employees.add(employee);
		
		petStore.setEmployees(employees);
		
		Employee dbEmployee = employeeDao.save(employee);	
		
	
		return new PetStoreEmployee(dbEmployee);
	}

	private Employee findEmployeeById(Long petStoreId, Long employeeId) {
		
		Employee employee = employeeDao.findById(employeeId).orElseThrow(() -> new NoSuchElementException(
				"Employee with ID=" + employeeId + " was not found."));
		
		if(employee.getPetStore().getPetStoreId() == petStoreId) {
			return employee;
		}else {
			throw new IllegalStateException("Employee " + employee.getEmployeeFirstName() +
					" does not work in pet store ID= " + petStoreId);
		}
		
	}
	
	private Employee findOrCreateEmployee(Long employeeId, Long petStoreId) {
		Employee employee;
		
		if(Objects.isNull(employeeId)) {
			employee = new Employee();
		}else {
			employee = findEmployeeById(petStoreId, employeeId);
		}
		return employee;
	}
	
	private void copyEmployeeFields(Employee employee, PetStoreEmployee petStoreEmployee) {
		employee.setEmployeeFirstName(petStoreEmployee.getEmployeeFirstName());
		employee.setEmployeeJobTitle(petStoreEmployee.getEmployeeJobTitle());
		employee.setEmployeeLastName(petStoreEmployee.getEmployeeLastName());
		employee.setEmployeePhone(petStoreEmployee.getEmployeePhone());
		employee.setEmployeeId(petStoreEmployee.getEmployeeId());
	}
	
	@Transactional(readOnly = false)
	public PetStoreCustomer saveCustomer(Long petStoreId, PetStoreCustomer petStoreCustomer) {
		
		Set<PetStore> petStores = new HashSet<PetStore>();
		PetStore petStore = findPetStoreById(petStoreId);
		petStores.add(petStore);
		Long customerId = petStoreCustomer.getCustomerId();
		Customer customer = findOrCreateCustomer(customerId, petStoreId);
		//wow make sure to keep the inputs into the methods in the correct order.
		copyCustomerFields(customer, petStoreCustomer);
		
		customer.setPetStores(petStores);
		Set<Customer> customers = new HashSet<Customer>();
		customers.add(customer);
		petStore.setCustomers(customers);
		
		Customer dbCustomer = customerDao.save(customer);
		//reference on calling an inner class 
		//https://docs.oracle.com/javase/tutorial/java/javaOO/nested.html
		
		return new PetStoreCustomer(dbCustomer);
				
	}
	
	private Customer findCustomerById(Long petStoreId, Long customerId) {
		Customer customer = customerDao.findById(customerId)
				.orElseThrow(() -> new NoSuchElementException(
				"Customer with ID=" + customerId + " was not found."));
	
		boolean boo = false;
		for(PetStore petStore : customer.getPetStores()) {
			if (petStore.getPetStoreId() == petStoreId) {
				boo = true;
			}
		}
		
		if (boo) {
			return customer;
		}else {
			throw new IllegalArgumentException("Customer " + customer.getCustomerFirstName() + " "
					+ customer.getCustomerLastName() + "did not shop at store Id= " + petStoreId);
		}
	
		
	}
	//wow make sure to keep the inputs into the methods in the correct order.
	private Customer findOrCreateCustomer(Long customerId, Long petStoreId) {
		Customer customer;
		
		if(Objects.isNull(customerId)){
			customer = new Customer();
		}else {
		customer = findCustomerById(petStoreId, customerId);
		}
		return customer;
	}
	
	private void copyCustomerFields(Customer customer, PetStoreCustomer petStoreCustomer) {
		customer.setCustomerEmail(petStoreCustomer.getCustomerEmail());
		customer.setCustomerFirstName(petStoreCustomer.getCustomerFirstName());
		customer.setCustomerLastName(petStoreCustomer.getCustomerLastName());
		
	}
	
	@Transactional(readOnly = true)
	public List<PetStoreData> retrieveAllPetStores() {
		List<PetStore> petStores = new LinkedList<>();
		petStores = petStoreDao.findAll();
		
		List<PetStoreData> result = new LinkedList<>();
		
		for(PetStore petStore : petStores) {
			PetStoreData psd = new PetStoreData(petStore);
			
			psd.getCustomers().clear();
			psd.getEmployees().clear();
			
			result.add(psd);
		}
		
		return result;
	}
	
	@Transactional(readOnly = true)
	public PetStoreData retrievePetStoreById(Long petStoreId) {
		PetStore petStore = findPetStoreById(petStoreId);
		petStore.getCustomers().clear();
		petStore.getEmployees().clear();
		
		return new PetStoreData(petStore);
	}

	public void deletePetStoreById(Long petStoreId) {
		PetStore petStore = findPetStoreById(petStoreId);
		
		petStoreDao.delete(petStore);
		
		
	}
	
}
